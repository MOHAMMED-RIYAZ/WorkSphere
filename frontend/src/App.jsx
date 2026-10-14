import { useEffect, useState } from "react";
import api from "./api";
import "./App.css";

function App() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [user, setUser] = useState(() => {
    const token = localStorage.getItem("worksphereToken");

    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));

      return {
        name: payload.name || payload.sub || "User",
        email: payload.email || payload.sub || "",
        token,
      };
    } catch {
      localStorage.removeItem("worksphereToken");
      return null;
    }
  });

  const [tasks, setTasks] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [currentPage, setCurrentPage] = useState(1);

  const [sortBy, setSortBy] = useState("NEWEST");

  const [tasksLoading, setTasksLoading] = useState(false);
  const [taskError, setTaskError] = useState("");

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");

  const [dueDate, setDueDate] = useState("");

  const [creatingTask, setCreatingTask] = useState(false);
  const [createError, setCreateError] = useState("");

  const [editingTaskId, setEditingTaskId] = useState(null);
  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [savingEdit, setSavingEdit] = useState(false);

  // Load tasks when the user signs in.
  useEffect(() => {
    if (!user) return;

    const fetchTasks = async () => {
      setTasksLoading(true);
      setTaskError("");

      try {
        const response = await api.get("/tasks");
        const data = response.data;

        setTasks(Array.isArray(data) ? data : (data.content ?? []));
      } catch (err) {
        console.error("Failed to load tasks:", err.response?.status);

        setTaskError(
          err.response?.status === 401
            ? "Your session is invalid. Please sign in again."
            : "Unable to load tasks. Please try again.",
        );
      } finally {
        setTasksLoading(false);
      }
    };

    fetchTasks();
  }, [user]);

  // Login.
  const handleLogin = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await api.post("/auth/login", {
        email: email.trim(),
        password,
      });

      const data = response.data;

      localStorage.setItem("worksphereToken", data.token);
      setUser(data);
    } catch (err) {
      setError(
        err.response?.status === 401
          ? "Invalid email or password."
          : "Unable to connect to the server.",
      );
    } finally {
      setLoading(false);
    }
  };

  // Create a task.
  const handleCreateTask = async (event) => {
    event.preventDefault();
    setCreateError("");
    setCreatingTask(true);

    try {
      const response = await api.post("/tasks", {
        title: title.trim(),
        description: description.trim(),
        dueDate: dueDate || null,
      });

      setTasks((previousTasks) => [response.data, ...previousTasks]);

      setTitle("");
      setDescription("");
      setDueDate("");
      setCurrentPage(1);
    } catch (err) {
      console.error(
        "Create task failed:",
        err.response?.status,
        err.response?.data,
      );

      setCreateError(
        err.response?.status === 401
          ? "Your session expired. Please sign in again."
          : err.response?.data?.message ||
              "Unable to create task. Please try again.",
      );
    } finally {
      setCreatingTask(false);
    }
  };

  // Change task status.
  const handleStatusChange = async (task, newStatus) => {
    setTaskError("");

    try {
      const response = await api.put(`/tasks/${task.id}`, {
        title: task.title,
        description: task.description || "",
        status: newStatus,
        dueDate: task.dueDate || null,
      });

      setTasks((previousTasks) =>
        previousTasks.map((item) =>
          item.id === task.id ? response.data : item,
        ),
      );
    } catch (err) {
      console.error(
        "Update task failed:",
        err.response?.status,
        err.response?.data,
      );

      setTaskError(
        err.response?.data?.message ||
          "Unable to update task status. Please try again.",
      );
    }
  };

  // Start editing a task.
  const handleEditTask = (task) => {
    setEditingTaskId(task.id);
    setEditTitle(task.title);
    setEditDescription(task.description || "");
    setDueDate(task.dueDate || "");
    setTaskError("");
  };

  // Save task changes.
  const handleSaveEdit = async (task) => {
    if (!editTitle.trim()) {
      setTaskError("Task title cannot be empty.");
      return;
    }

    setSavingEdit(true);
    setTaskError("");

    try {
      const response = await api.put(`/tasks/${task.id}`, {
        title: editTitle.trim(),
        description: editDescription.trim(),
        status: task.status,
        dueDate: dueDate || null,
      });

      setTasks((previousTasks) =>
        previousTasks.map((item) =>
          item.id === task.id ? response.data : item,
        ),
      );

      setEditingTaskId(null);
      setEditTitle("");
      setEditDescription("");
    } catch (err) {
      console.error(
        "Edit task failed:",
        err.response?.status,
        err.response?.data,
      );

      setTaskError(
        err.response?.data?.message ||
          "Unable to save task changes. Please try again.",
      );
    } finally {
      setSavingEdit(false);
    }
  };

  // Delete a task.
  const handleDeleteTask = async (taskId) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this task?",
    );

    if (!confirmed) return;

    setTaskError("");

    try {
      await api.delete(`/tasks/${taskId}`);

      setTasks((previousTasks) =>
        previousTasks.filter((task) => task.id !== taskId),
      );
    } catch (err) {
      console.error("Failed to delete task:", err);

      setTaskError(
        err.response?.data?.message ||
          "Failed to delete task. Please try again.",
      );
    }
  };

  // Logout.
  const handleLogout = () => {
    localStorage.removeItem("worksphereToken");
    setUser(null);
    setTasks([]);
    setPassword("");
    setError("");
    setTaskError("");
    setCreateError("");
    setSearchTerm("");
    setStatusFilter("ALL");
    setCurrentPage(1);
  };

  // Search and filter tasks.
  const filteredTasks = tasks.filter((task) => {
    const query = searchTerm.trim().toLowerCase();

    const matchesSearch =
      (task.title || "").toLowerCase().includes(query) ||
      (task.description || "").toLowerCase().includes(query);

    const matchesStatus =
      statusFilter === "ALL" || task.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  const sortedTasks = [...filteredTasks].sort((a, b) => {
    switch (sortBy) {
      case "OLDEST":
        return Number(a.id) - Number(b.id);

      case "TITLE":
        return (a.title || "").localeCompare(b.title || "");

      case "NEWEST":
      default:
        return Number(b.id) - Number(a.id);
    }
  });

  // Pagination: show five tasks per page.
  const tasksPerPage = 5;

  const totalPages = Math.max(
    1,
    Math.ceil(filteredTasks.length / tasksPerPage),
  );

  // Keep the current page valid after filtering or deleting tasks.
  useEffect(() => {
    setCurrentPage((page) => Math.min(page, totalPages));
  }, [totalPages]);

  const paginatedTasks = sortedTasks.slice(
    (currentPage - 1) * tasksPerPage,
    currentPage * tasksPerPage,
  );

  const totalTaskCount = tasks.length;

  const pendingTaskCount = tasks.filter(
    (task) => task.status === "TODO",
  ).length;

  const inProgressTaskCount = tasks.filter(
    (task) => task.status === "IN_PROGRESS",
  ).length;

  const completedTaskCount = tasks.filter(
    (task) => task.status === "DONE",
  ).length;

  if (user) {
    return (
      <div className="app-container">
        <div className="dashboard-card">
          <header className="dashboard-header">
            <div>
              <h1>WorkSphere</h1>
              <p className="subtitle">Your workspace, organized.</p>
            </div>

            <button className="logout-button" onClick={handleLogout}>
              Sign out
            </button>
          </header>

          <section className="welcome-section">
            <h2>Welcome, {user.name}!</h2>
            <p>{user.email}</p>
          </section>

          <section className="dashboard-stats">
            <div className="stat-card total-stat">
              <p>Total Tasks</p>
              <h3>{totalTaskCount}</h3>
            </div>

            <div className="stat-card pending-stat">
              <p>Pending Tasks</p>
              <h3>{pendingTaskCount}</h3>
            </div>

            <div className="stat-card progress-stat">
              <p>In Progress</p>
              <h3>{inProgressTaskCount}</h3>
            </div>

            <div className="stat-card completed-stat">
              <p>Completed Tasks</p>
              <h3>{completedTaskCount}</h3>
            </div>
          </section>

          <section className="create-task-section">
            <h2>Create a new task</h2>

            <form onSubmit={handleCreateTask} className="create-task-form">
              <label htmlFor="task-title">Task title</label>

              <input
                id="task-title"
                type="text"
                placeholder="e.g. Complete API documentation"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                maxLength={150}
                required
              />

              <label htmlFor="task-description">Description</label>

              <textarea
                id="task-description"
                placeholder="Describe what needs to be done..."
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                rows={3}
              />

              <label htmlFor="dueDate">Due Date (Optional)</label>
              <input
                id="dueDate"
                type="date"
                value={dueDate}
                onChange={(event) => setDueDate(event.target.value)}
              />

              {createError && (
                <p className="error-message" role="alert">
                  {createError}
                </p>
              )}

              <button type="submit" disabled={creatingTask}>
                {creatingTask ? "Creating..." : "Create Task"}
              </button>
            </form>
          </section>

          <section className="tasks-section">
            <div className="tasks-heading">
              <h2>Your Tasks</h2>
              <span className="task-count">{tasks.length} tasks</span>
            </div>

            <div className="task-filters">
              <input
                type="text"
                placeholder="Search tasks by title or description..."
                value={searchTerm}
                onChange={(event) => {
                  setSearchTerm(event.target.value);
                  setCurrentPage(1);
                }}
              />

              {/* Filter by status */}
              <select
                value={statusFilter}
                onChange={(event) => {
                  setStatusFilter(event.target.value);
                  setCurrentPage(1);
                }}
                aria-label="Filter tasks by status"
              >
                <option value="ALL">All statuses</option>
                <option value="TODO">To do</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="DONE">Done</option>
              </select>

              {/* Sort tasks */}
              <select
                value={sortBy}
                onChange={(event) => {
                  setSortBy(event.target.value);
                  setCurrentPage(1);
                }}
                aria-label="Sort tasks"
              >
                <option value="NEWEST">Newest first</option>
                <option value="OLDEST">Oldest first</option>
                <option value="TITLE">Title (A–Z)</option>
              </select>
            </div>

            <p className="task-count">
              Showing {filteredTasks.length} of {tasks.length} tasks
            </p>

            {tasksLoading && <p>Loading tasks...</p>}

            {taskError && (
              <p className="error-message" role="alert">
                {taskError}
              </p>
            )}

            {!tasksLoading && !taskError && filteredTasks.length === 0 && (
              <p>No tasks found. Your workspace is ready!</p>
            )}

            {!tasksLoading && !taskError && filteredTasks.length > 0 && (
              <>
                <div className="task-list">
                  {paginatedTasks.map((task) => (
                    <article className="task-item" key={task.id}>
                      {editingTaskId === task.id ? (
                        <div className="edit-task-form">
                          <label htmlFor={`edit-title-${task.id}`}>
                            Task title
                          </label>

                          <input
                            id={`edit-title-${task.id}`}
                            type="text"
                            value={editTitle}
                            onChange={(event) =>
                              setEditTitle(event.target.value)
                            }
                            maxLength={150}
                            required
                          />

                          <label htmlFor={`edit-description-${task.id}`}>
                            Description
                          </label>

                          <textarea
                            id={`edit-description-${task.id}`}
                            value={editDescription}
                            onChange={(event) =>
                              setEditDescription(event.target.value)
                            }
                            rows={3}
                          />

                          <label htmlFor={`edit-due-date-${task.id}`}>
                            Due Date (Optional)
                          </label>

                          <input
                            id={`edit-due-date-${task.id}`}
                            type="date"
                            value={dueDate}
                            onChange={(event) => setDueDate(event.target.value)}
                          />

                          <div className="task-actions">
                            <button
                              type="button"
                              onClick={() => handleSaveEdit(task)}
                              disabled={savingEdit}
                            >
                              {savingEdit ? "Saving..." : "Save"}
                            </button>

                            <button
                              type="button"
                              onClick={() => {
                                setEditingTaskId(null);
                                setEditTitle("");
                                setEditDescription("");
                                setTaskError("");
                              }}
                              disabled={savingEdit}
                            >
                              Cancel
                            </button>
                          </div>
                        </div>
                      ) : (
                        <div className="task-details">
                          <h3>{task.title}</h3>

                          <p>
                            {task.description || "No description provided."}
                          </p>

                          {task.dueDate && (
                            <div className="task-due-date">
                              <p>
                                Due date:{" "}
                                {new Date(
                                  `${task.dueDate}T00:00:00`,
                                ).toLocaleDateString("en-IN", {
                                  day: "2-digit",
                                  month: "short",
                                  year: "numeric",
                                })}
                              </p>

                              {task.dueDate <
                                new Date().toLocaleDateString("en-CA") &&
                                task.status !== "DONE" && (
                                  <span className="overdue-badge">Overdue</span>
                                )}
                            </div>
                          )}

                          <button
                            type="button"
                            onClick={() => handleEditTask(task)}
                          >
                            Edit
                          </button>
                        </div>
                      )}

                      <select
                        className={`task-status ${String(
                          task.status || "TODO",
                        ).toLowerCase()}`}
                        value={task.status || "TODO"}
                        onChange={(event) =>
                          handleStatusChange(task, event.target.value)
                        }
                        disabled={editingTaskId === task.id || savingEdit}
                        aria-label={`Status for ${task.title}`}
                      >
                        <option value="TODO">TODO</option>
                        <option value="IN_PROGRESS">IN_PROGRESS</option>
                        <option value="DONE">DONE</option>
                      </select>

                      <button
                        type="button"
                        className="delete-button"
                        onClick={() => handleDeleteTask(task.id)}
                        disabled={editingTaskId === task.id || savingEdit}
                      >
                        Delete
                      </button>
                    </article>
                  ))}
                </div>

                <div className="pagination-controls">
                  <button
                    type="button"
                    onClick={() =>
                      setCurrentPage((page) => Math.max(1, page - 1))
                    }
                    disabled={currentPage === 1}
                  >
                    Previous
                  </button>

                  <span>
                    Page {currentPage} of {totalPages}
                  </span>

                  <button
                    type="button"
                    onClick={() =>
                      setCurrentPage((page) => Math.min(totalPages, page + 1))
                    }
                    disabled={currentPage === totalPages}
                  >
                    Next
                  </button>
                </div>
              </>
            )}
          </section>
        </div>
      </div>
    );
  }

  // Login screen.
  return (
    <div className="app-container">
      <div className="login-card">
        <h1>WorkSphere</h1>
        <p className="subtitle">Your workspace, organized.</p>

        <form onSubmit={handleLogin}>
          <label htmlFor="email">Email address</label>

          <input
            id="email"
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          <label htmlFor="password">Password</label>

          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          {error && (
            <p role="alert" className="error-message">
              {error}
            </p>
          )}

          <button type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="footer-text">
          Manage tasks. Collaborate. Get things done.
        </p>
      </div>
    </div>
  );
}

export default App;

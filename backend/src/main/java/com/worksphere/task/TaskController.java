
package com.worksphere.task;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody Task task) {
        return taskRepository.save(task);
    }
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return findTask(id);
    }
@GetMapping
public Page<Task> getAllTasks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "id") String sortBy,
@RequestParam(defaultValue = "asc") String direction) {

    if (page < 0 || size < 1 || size > 100) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page must be non-negative and size must be between 1 and 100");
    }

  if (!sortBy.equals("id") && !sortBy.equals("title")
        && !sortBy.equals("status")) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "sortBy must be id, title, or status");
}

Sort.Direction sortDirection;

try {
    sortDirection = Sort.Direction.fromString(direction);
} catch (IllegalArgumentException ex) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "direction must be asc or desc");
}

Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by(sortDirection, sortBy)
);

    if (status != null) {
        return taskRepository.findByStatus(status, pageable);
    }

    if (search != null && !search.isBlank()) {
        return taskRepository.findByTitleContainingIgnoreCase(
                search.trim(), pageable);
    }
    return taskRepository.findAll(pageable);
}
@PutMapping("/{id}")
public Task updateTask(
        @PathVariable Long id,
        @Valid @RequestBody Task updatedTask) {

    Task existingTask = findTask(id);
    existingTask.setTitle(updatedTask.getTitle());
    existingTask.setDescription(updatedTask.getDescription());
    existingTask.setDueDate(updatedTask.getDueDate());
    existingTask.setStatus(updatedTask.getStatus());

    return taskRepository.save(existingTask);
}

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"));
    }
}

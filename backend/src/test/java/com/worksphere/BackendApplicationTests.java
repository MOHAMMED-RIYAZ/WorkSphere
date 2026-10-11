
package com.worksphere;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.mockito.Mockito.verify;
import com.worksphere.task.Task;
import com.worksphere.task.TaskController;
import com.worksphere.task.TaskRepository;
import com.worksphere.task.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.worksphere.JwtService;
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
private JwtService jwtService;

    @MockitoBean
    private TaskRepository taskRepository;

    @Test
    void createTaskReturnsCreatedTask() throws Exception {
        Task task = new Task();
        task.setTitle("Learn Spring Boot");
        task.setDescription("Build WorkSphere");
        task.setStatus(TaskStatus.TODO);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Learn Spring Boot",
                                  "description": "Build WorkSphere",
                                  "status": "TODO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }
	
    @Test
    void createTaskWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Task without a title",
                                  "status": "TODO"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
	
    @Test
    void getTasksWithSearchReturnsMatchingTasks() throws Exception {
        Task task = new Task();
        task.setTitle("Add User Authentication");
        task.setDescription("Secure WorkSphere APIs");
        task.setStatus(TaskStatus.TODO);

        when(taskRepository.findByTitleContainingIgnoreCase(
                org.mockito.ArgumentMatchers.eq("Authentication"),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("search", "Authentication"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Add User Authentication"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getTasksWithStatusReturnsMatchingTasks() throws Exception {
        Task task = new Task();
        task.setTitle("Add User Authentication");
        task.setDescription("Secure WorkSphere APIs");
        task.setStatus(TaskStatus.TODO);

        when(taskRepository.findByStatus(
                org.mockito.ArgumentMatchers.eq(TaskStatus.TODO),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getTasksWithPaginationReturnsPageMetadata() throws Exception {
        Task task = new Task();
        task.setTitle("Learn Spring Boot");
        task.setDescription("Build WorkSphere");
        task.setStatus(TaskStatus.TODO);

        when(taskRepository.findAll(
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(task),
                        PageRequest.of(0, 1),
                        3
                ));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("page", "0")
                            .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }



    @Test
    void getTasksWithSortingUsesRequestedSortOrder() throws Exception {
        when(taskRepository.findAll(
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("sortBy", "title")
                            .param("direction", "desc"))
                .andExpect(status().isOk());

        verify(taskRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<org.springframework.data.domain.Pageable>argThat(
                                pageable ->
                                        pageable.getSort()
                                                .getOrderFor("title") != null
                                                && pageable.getSort()
                                                        .getOrderFor("title")
                                                        .isDescending()));
    }

    @Test
    void getTasksWithInvalidSortFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("sortBy", "password"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTaskByIdReturnsTaskWhenFound() throws Exception {
        Task task = new Task();
        task.setTitle("Learn Spring Boot");
        task.setDescription("Build WorkSphere");
        task.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void getTaskByIdReturnsNotFoundWhenMissing() throws Exception {
        when(taskRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskReturnsUpdatedTask() throws Exception {
        Task existingTask = new Task();
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old description");
        existingTask.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(existingTask));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Updated Title",
                                      "description": "Updated description",
                                      "status": "IN_PROGRESS"
                                    }
                                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteTaskReturnsNoContentWhenTaskExists() throws Exception {
        Task task = new Task();
        task.setTitle("Task to Delete");
        task.setDescription("Verify delete endpoint");
        task.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskRepository).delete(task);
    }

    @Test
    void updateMissingTaskReturnsNotFound() throws Exception {
        when(taskRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .put("/api/tasks/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Updated Title",
                                      "description": "Updated description",
                                      "status": "TODO"
                                    }
                                    """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMissingTaskReturnsNotFound() throws Exception {
        when(taskRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/tasks/999"))
                .andExpect(status().isNotFound());

        org.mockito.Mockito.verify(taskRepository, org.mockito.Mockito.never())
                .delete(any(Task.class));
    }

    @Test
    void getTasksWithInvalidStatusReturnsBadRequest() throws Exception {
        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTasksWithInvalidSortDirectionReturnsBadRequest()
            throws Exception {
        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("direction", "sideways"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTasksWithNegativePageReturnsBadRequest() throws Exception {
        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("page", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTasksWithZeroPageSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get("/api/tasks")
                            .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTaskWithoutTitleReturnsBadRequest() throws Exception {
        Task existingTask = new Task();
        existingTask.setTitle("Existing Title");
        existingTask.setDescription("Existing description");
        existingTask.setStatus(TaskStatus.TODO);

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(existingTask));

        mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .put("/api/tasks/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "description": "Updated description",
                                      "status": "TODO"
                                    }
                                    """))
                .andExpect(status().isBadRequest());
    }

}

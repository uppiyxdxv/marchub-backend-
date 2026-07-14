package com.marchub.controller;

import com.marchub.dto.CourseRequest;
import com.marchub.model.Course;
import com.marchub.service.MarchubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final MarchubService service;

    public CourseController(MarchubService service) {
        this.service = service;
    }

    @GetMapping
    public List<Course> getAll() {
        return service.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable Long id) {
        return service.getCourse(id);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody CourseRequest req) {
        return service.createCourse(req);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody CourseRequest req) {
        return service.updateCourse(id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        return service.deleteCourse(id);
    }
}

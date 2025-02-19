package com.coolSchool.CoolSchool.controllers;

import com.coolSchool.CoolSchool.filters.JwtAuthenticationFilter;
import com.coolSchool.CoolSchool.models.dto.auth.PublicUserDTO;
import com.coolSchool.CoolSchool.models.dto.request.BlogRequestDTO;
import com.coolSchool.CoolSchool.models.dto.response.BlogResponseDTO;
import com.coolSchool.CoolSchool.services.BlogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/blogs")
public class BlogController {
    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<BlogResponseDTO>> getAllBlogs(HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(blogService.getAllBlogs((PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey)));
    }

    @PostMapping("/like/{blogId}")
    public ResponseEntity<BlogResponseDTO> likeBlog(@PathVariable Long blogId, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(blogService.addLike(blogId, (PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> getBlogById(@PathVariable(name = "id") Long id, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(blogService.getBlogById(id, (PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey)));
    }

    @PostMapping("/create")
    public ResponseEntity<BlogResponseDTO> createBlog(@Valid @RequestBody BlogRequestDTO blogDTO, HttpServletRequest httpServletRequest) {
        BlogResponseDTO cratedBlog = blogService.createBlog(blogDTO, (PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey));
        return new ResponseEntity<>(cratedBlog, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponseDTO> updateBlog(@PathVariable("id") Long id, @Valid @RequestBody BlogRequestDTO blogDTO, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(blogService.updateBlog(id, blogDTO, (PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBlogById(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        blogService.deleteBlog(id, (PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey));
        return ResponseEntity.ok("Blog with id: " + id + " has been deleted successfully!");
    }

    @GetMapping("/sort/default")
    public ResponseEntity<List<BlogResponseDTO>> getBlogsByNewest() {
        return ResponseEntity.ok(blogService.getBlogsByNewestFirst());
    }

    @GetMapping("/sort/likes")
    public ResponseEntity<List<BlogResponseDTO>> getBlogsByNumberOfLikes() {
        return ResponseEntity.ok(blogService.getBlogsByMostLiked());
    }

    @GetMapping("/search/all")
    public ResponseEntity<List<BlogResponseDTO>> searchBlogs(@RequestParam("title") Optional<String> title, @RequestParam("category") Optional<String> category,
                                                             @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort, HttpServletRequest httpServletRequest) {
        List<BlogResponseDTO> result;
        if (title.isPresent() && category.isPresent()) {
            result = blogService.searchBlogsByKeywordInTitleAndCategory(title.get(), category.get());
        } else if (title.isPresent()) {
            result = blogService.searchBlogsByKeywordTitle(title.get());
        } else if (category.isPresent()) {
            result = blogService.searchBlogsByKeywordCategory(category.get());
        } else {
            result = blogService.getAllBlogs((PublicUserDTO) httpServletRequest.getAttribute(JwtAuthenticationFilter.userKey));
        }

        List<BlogResponseDTO> mutableResult = new ArrayList<>(result);
        if ("newest".equals(sort)) {
            mutableResult.sort(Comparator.comparing(BlogResponseDTO::getCreated_at).reversed());
            return ResponseEntity.ok(mutableResult);
        }

        if ("mostLiked".equals(sort)) {
            Comparator<BlogResponseDTO> sortByMostLiked = Comparator.comparingInt(blog -> blog.getLiked_users().size());
            mutableResult.sort(sortByMostLiked.reversed());
            return ResponseEntity.ok(mutableResult);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/mostRecent/{n}")
    public ResponseEntity<List<BlogResponseDTO>> getLastNRecentBlogs(@PathVariable("n") int n) {
        return ResponseEntity.ok(blogService.getLastNBlogs(n));
    }
}

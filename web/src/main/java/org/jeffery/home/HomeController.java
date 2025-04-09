package org.jeffery.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    // Placeholder data class for articles
    public static class Article {
        private final String title;
        private final String author;
        private final String summary;

        public Article(String title, String author, String summary) {
            this.title = title;
            this.author = author;
            this.summary = summary;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getSummary() {
            return summary;
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        // Add static article data for the new layout
        List<Article> topArticles = Arrays.asList(
                new Article("Spring Boot Microservices", "Michael Lee", "Building scalable microservices with Spring Boot and Spring Cloud."),
                new Article("Advanced SQL Techniques", "Sarah Chen", "Optimize your database queries with these advanced SQL tips."),
                new Article("Effective Java Programming", "David Kim", "Best practices for writing robust and maintainable Java code."),
                new Article("Docker for Developers", "Laura Green", "Containerize your applications easily using Docker.")
        );

        List<Article> recentArticles = Arrays.asList(
                new Article("Getting Started with Spring Boot 3", "John Doe", "A comprehensive guide to setting up your first Spring Boot application."),
                new Article("Mastering Thymeleaf Templates", "Jane Smith", "Learn advanced techniques for building dynamic web pages with Thymeleaf."),
                new Article("Understanding REST APIs", "Peter Jones", "An introduction to the principles and best practices of RESTful API design."),
                new Article("Introduction to RabbitMQ", "Alice Brown", "Explore the basics of message queuing with RabbitMQ and Spring AMQP."),
                new Article("Redis for Caching", "Bob White", "Improve your application performance by implementing caching with Redis."),
                new Article("Web Security Fundamentals", "Charlie Davis", "Protect your web applications from common security threats."),
                new Article("Introduction to Kubernetes", "Emily Clark", "Orchestrate your containerized applications with Kubernetes.")
        );
        model.addAttribute("topArticles", topArticles); // For the top card grid
        model.addAttribute("recentArticles", recentArticles); // For the main list
        return "index"; // This will look for index.html in templates folder
    }
} 
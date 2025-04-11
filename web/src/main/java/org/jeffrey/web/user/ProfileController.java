package org.jeffrey.web.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    /**
     * Renders the user profile page.
     *
     * @param model The Spring UI Model.
     * @return The name of the profile view template.
     */
    @GetMapping("/profile")
    public String profilePage(Model model) {
        model.addAttribute("title", "DevSpace - Profile");
        model.addAttribute("currentPage", "profile");
        model.addAttribute("viewName", "profile");
        // Flag to indicate this page requires authentication
        model.addAttribute("requiresAuth", true);
        return "layout/main";
    }

    /**
     * Renders the settings page.
     *
     * @param model The Spring UI Model.
     * @return The name of the settings view template.
     */
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        model.addAttribute("title", "DevSpace - Settings");
        model.addAttribute("currentPage", "settings");
        // Flag to indicate this page requires authentication
        model.addAttribute("requiresAuth", true);
        return "redirect:/profile#settings";
    }
} 
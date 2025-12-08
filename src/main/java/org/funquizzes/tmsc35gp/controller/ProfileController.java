package org.funquizzes.tmsc35gp.controller;

import org.funquizzes.tmsc35gp.dto.ChangePasswordDto;
import org.funquizzes.tmsc35gp.dto.UpdateProfileDto;
import org.funquizzes.tmsc35gp.entity.User;
import org.funquizzes.tmsc35gp.entity.UserStatistic;
import org.funquizzes.tmsc35gp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    // main profile page (обзор)
    @GetMapping
    public String profileOverview(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "overview");
        return "profile/overview";
    }

    //edit profile
    @GetMapping("/edit")
    public String profileEdit(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());

        UpdateProfileDto updateProfileDto = new UpdateProfileDto();
        updateProfileDto.setName(user.getName());
        updateProfileDto.setUsername(user.getUsername());
        updateProfileDto.setEmail(user.getEmail());
        updateProfileDto.setBiography(user.getBiography());
        updateProfileDto.setPublicProfile(user.isPublicProfile());

        model.addAttribute("updateProfileDto", updateProfileDto);
        model.addAttribute("activePage", "edit");
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String profileEdit(@ModelAttribute("updateProfileDto") UpdateProfileDto dto, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(authentication.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Информация профиля успешно обновлена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при обновлении профиля!");
        }
        return "redirect:/users/profile";
    }

    //change password
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("passwordDto", new ChangePasswordDto());
        model.addAttribute("activePage", "change-password");
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute ChangePasswordDto dto, Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean success = userService.changePassword(authentication.getName(), dto);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Пароль успешно изменён");
        } else {
            redirectAttributes.addFlashAttribute("message", "Неверный текущий пароль или пароли не совпадают");
        }
        return "redirect:/users/profile";
    }

    //statistic
    @GetMapping("/statistic")
    public String statistic(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        UserStatistic statistic = userService.getUserStatistics(authentication.getName());

        model.addAttribute("user", user);
        model.addAttribute("statistic", statistic);
        model.addAttribute("activePage", "statistic");

        List<UserStatistic> topPlayers = userService.getTopUsersByScore(10);
        model.addAttribute("topPlayers", topPlayers);

        // Рассчитываем проценты если есть данные
        if (statistic.getTotalQuizzesPlayed() > 0) {
            int accuracy = (statistic.getTotalCorrectAnswers() * 100) / (statistic.getTotalQuizzesPlayed() * 10);
            model.addAttribute("accuracy", accuracy);
        }

        return "profile/statistic";
    }

    //friends
    @GetMapping("/friends")
    public String friends(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "friends");
        return "profile/friends";
    }

    //add friend
    @PostMapping("/friends/add/{friendId}")
    public String addFriend (@PathVariable Long friendId, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.addFriend(authentication.getName(), friendId);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь добавлен в Друзья");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при попытке добавить пользователя в Друзья");
        }
        return "redirect:/users/profile/friends";
    }

    //delete friend
    @PostMapping("/friends/remove/{friendId}")
    public String removeFriend (@PathVariable Long friendId, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.removeFriend(authentication.getName(), friendId);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь удалён из списка друзей");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка при удалении пользователя из списка друзей");
        }
        return "redirect:/users/profile/friends";
    }

    //settings
    @GetMapping ("/settings")
    public String settings(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "settings");
        return "profile/settings";
    }

    //notifications
    @PostMapping("/settings/notifications")
    public String toggleNotifications(@RequestParam boolean receiveNotifications,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        user.setReceiveNotifications(receiveNotifications);
        userService.updateProfile(authentication.getName(),
                new UpdateProfileDto(user.getName(), user.getUsername(), user.getEmail(),
                        user.getBiography(), user.isPublicProfile()));

        redirectAttributes.addFlashAttribute("successMessage", "Настройки уведомлений обновлены");
        return "redirect:/users/profile/settings";
    }

    //achievements
    @GetMapping("/achievements")
    public String achievements(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "achievements");
        return "profile/achievements";
    }

    //activity
    @GetMapping("/activity")
    public String activity(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "activity");
        return "profile/activity";
    }

    //game history
    @GetMapping("/game-history")
    public String history(Authentication authentication, Model model) {
        User user = (User) userService.loadUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "game-history");
        return "profile/game-history";
    }
}
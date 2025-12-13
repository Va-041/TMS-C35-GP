//package org.funquizzes.tmsc35gp.controller;
//
//import org.funquizzes.tmsc35gp.entity.Category;
//import org.funquizzes.tmsc35gp.service.CategoryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/categories")
//public class CategoryController {
//
//
//    @Autowired
//    private CategoryService categoryService;
//
//    @GetMapping
//    public String listCategories(Model model) {
//        List<Category> categories = categoryService.getAllActiveCategories();
//        model.addAttribute("categories", categories);
//        return "admin/categories/list";
//    }
//
//    @GetMapping("/create")
//    public String showCreateForm(Model model) {
//        model.addAttribute("category", new Category());
//        return "admin/categories/create";
//    }
//
//    @PostMapping("/create")
//    public String createCategory(@ModelAttribute Category category,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            categoryService.createCategory(category);
//            redirectAttributes.addFlashAttribute("success", "Категория успешно создана");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/admin/categories";
//    }
//
//    @GetMapping("/edit/{id}")
//    public String showEditForm(@PathVariable Long id, Model model) {
//        Category category = categoryService.getCategoryById(id);
//        model.addAttribute("category", category);
//        return "admin/categories/edit";
//    }
//
//    @PostMapping("/edit/{id}")
//    public String updateCategory(@PathVariable Long id,
//                                 @ModelAttribute Category category,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            categoryService.updateCategory(id, category);
//            redirectAttributes.addFlashAttribute("success", "Категория успешно обновлена");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/admin/categories";
//    }
//
//    @GetMapping("/delete/{id}")
//    public String deleteCategory(@PathVariable Long id,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            categoryService.deleteCategory(id);
//            redirectAttributes.addFlashAttribute("success", "Категория успешно удалена");
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/admin/categories";
//    }
//}

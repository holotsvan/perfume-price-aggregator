package com.aggregator.controller.ui;

import com.aggregator.service.PerfumeService;
import com.aggregator.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebUiController {

    private final PerfumeService perfumeService;
    private final StoreService storeService;

    @Autowired
    public WebUiController(PerfumeService perfumeService, StoreService storeService) {
        this.perfumeService = perfumeService;
        this.storeService = storeService;
    }

    @GetMapping({"/", "/perfumes"})
    public String perfumes(Model model) {
        model.addAttribute("perfumes", perfumeService.getAllPerfumes());
        return "perfumes";
    }

    @GetMapping("/perfumes/{id}")
    public String perfumeDetails(@PathVariable Long id, Model model) {
        model.addAttribute("perfume", perfumeService.getPerfumeDetails(id));
        model.addAttribute("stores", storeService.getAllStores());
        return "perfume-details";
    }

    @GetMapping("/add-perfume")
    public String addPerfume(Model model) {
        return "add-perfume";
    }

    @GetMapping("/stores")
    public String stores(Model model) {
        model.addAttribute("stores", storeService.getAllStores());
        return "stores";
    }
}

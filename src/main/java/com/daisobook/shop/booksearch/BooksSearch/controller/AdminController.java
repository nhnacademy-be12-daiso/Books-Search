//package com.daisobook.shop.booksearch.BooksSearch.controller;
//
//import com.daisobook.shop.booksearch.BooksSearch.service.ImageMigrationService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/admin")
//@RequiredArgsConstructor
//public class AdminController {
//    private final ImageMigrationService imageMigrationService;
//
//    @PostMapping("/migrate-images")
//    public String migrate() {
////        imageMigrationService.migrateAllImages();
//        imageMigrationService.migrateInBatches();
//        return "Migration process started. Check logs for details.";
//    }
//}
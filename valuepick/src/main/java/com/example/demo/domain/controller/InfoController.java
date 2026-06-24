package com.example.demo.domain.controller;

import com.example.demo.domain.service.SimpleInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/info")
public class InfoController {
    @Autowired
    SimpleInfoService simpleInfoService;

    // 저PER 상위 5
    @GetMapping(value = "/per",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> getPER() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPER());
    }
    // 저PBR 상위 5
    @GetMapping("/pbr")
    public ResponseEntity<List<Map<String, Object>>> getPBR() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPBR());
    }

    // 고ROE 상위 5
    @GetMapping("/roe")
    public ResponseEntity<List<Map<String, Object>>> getROE() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getROE());
    }
}

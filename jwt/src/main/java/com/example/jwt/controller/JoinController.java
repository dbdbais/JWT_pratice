package com.example.jwt.controller;

import com.example.jwt.dto.JoinDTO;
import com.example.jwt.service.JoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class JoinController {

    private final JoinService joinService;

    public JoinController(JoinService joinService) {
        this.joinService = joinService;
    }

    @PostMapping("/join")
    public String joinProcess( JoinDTO joinDTO){
        System.out.println(joinDTO.getUsername());
        System.out.println("----------");
        joinService.joinProcess(joinDTO);
        return "ok";
    }
}

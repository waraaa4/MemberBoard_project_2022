package com.ex.memberboard.controller;

import com.ex.memberboard.common.SessionConst;
import com.ex.memberboard.dto.MemberDetailDTO;
import com.ex.memberboard.dto.MemberLoginDTO;
import com.ex.memberboard.dto.MemberSaveDTO;
import com.ex.memberboard.dto.MemberUpdateDTO;
import com.ex.memberboard.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member/")
public class MemberController {

    private final MemberService ms;

    @GetMapping("save")
    public String saveForm(Model model) {
        model.addAttribute("member", new MemberSaveDTO());
        return "member/save";
    }

    @PostMapping("save")
    public String save(@Validated @ModelAttribute MemberSaveDTO memberSaveDTO) throws IllegalStateException, IOException {
        Long memberId = ms.save(memberSaveDTO);
        return "member/login";
    }

    @PostMapping("emailCheck")
    public ResponseEntity emailCheck(@RequestParam("memberEmail") String memberEmail) {
        String result = ms.emailCheck(memberEmail);
        if (result.equals("OK")) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("login")
    public String loginForm(Model model) {
        model.addAttribute("member", new MemberLoginDTO());
        return "member/login";
    }

    @GetMapping("logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "index";
    }

    @PostMapping("login")
    public ResponseEntity login(@RequestBody MemberLoginDTO memberLoginDTO, HttpSession session) {
        MemberDetailDTO memberDetailDTO = ms.findByEmail(memberLoginDTO);
        try {
            if (!memberDetailDTO.equals(null)) {
                if (memberDetailDTO.getMemberEmail().equals("admin")) {
                    session.setAttribute("loginId", memberDetailDTO.getMemberId());
                    session.setAttribute("loginEmail", memberDetailDTO.getMemberEmail());
                    return new ResponseEntity<String>("admin",HttpStatus.OK);
                }
                session.setAttribute("loginId", memberDetailDTO.getMemberId());
                session.setAttribute("loginEmail", memberDetailDTO.getMemberEmail());
                String redirectURL = (String) session.getAttribute("redirectURL");
                if (redirectURL != null) {
                    return new ResponseEntity<String>(redirectURL, HttpStatus.OK);

                } else {
                    return new ResponseEntity<String>("/board/", HttpStatus.OK);
                }
            } else {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (NullPointerException nullPointerException) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("mypage/{memberId}")
    public String mypageForm(@PathVariable Long memberId, Model model) {
        MemberDetailDTO memberDetailDTO = ms.findById(memberId);
        model.addAttribute("member",memberDetailDTO);
        return "member/mypage";
    }

    @PutMapping("{memberId}")
    public ResponseEntity update(@RequestBody MemberUpdateDTO memberUpdateDTO) {
        Long memberId = ms.update(memberUpdateDTO);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("admin")
    public String admin() {
        return "member/admin";
    }

    @GetMapping
    public String findAll(Model model) {
        List<MemberDetailDTO> memberList = ms.findAll();
        model.addAttribute("memberList", memberList);
        return "member/findAll";
    }

    @DeleteMapping("{memberId}")
    public ResponseEntity deleteById(@PathVariable Long memberId) {
        ms.deleteById(memberId);
        return  new ResponseEntity(HttpStatus.OK);
    }

}

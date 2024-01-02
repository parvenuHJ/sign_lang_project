package com.soon.slt.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import ch.qos.logback.core.net.SyslogOutputStream;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.soon.slt.entity.TbBoard;
import com.soon.slt.entity.TbComment;
import com.soon.slt.entity.TbUser;
import com.soon.slt.form.TbBoardForm;
import com.soon.slt.service.TbBoardService;
import com.soon.slt.service.TbCommentService;
import com.soon.slt.service.TbUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/board")
@Controller
@RequiredArgsConstructor
public class TbBoardController {

    private final TbBoardService tbBoardService;
    private final TbUserService tbUserService;
    private final TbCommentService tbCommentService;

    // Footer 와 Header 활성화
    @GetMapping("footer")
    public String footer() {
        return "footer";
    }
    @GetMapping("header")
    public String header() {
        return "header";
    }

    // 게시글 리스트 조회
    @GetMapping("/main")
    public String boardMain(Model model, @RequestParam(value = "page", defaultValue = "0")int page,
                            @RequestParam(value="filter", defaultValue = "") String filter) {
        Page<TbBoard> paging = this.tbBoardService.boardFilter(page, filter);
        model.addAttribute("paging", paging);
        return "board-list";
    }

    // 게시글 검색 조회
    @GetMapping("/search")
    public String boardSearch(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "category", defaultValue = "") String category,
                              @RequestParam(value = "kw", defaultValue = "") String kw) {
        Page<TbBoard> paging = this.tbBoardService.searchList(page, category, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "board-list";
    }

    // 게시글 카테고리별
    @GetMapping("/filter")
    @ResponseBody
    public Page<TbBoard> boardFilter(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "filter", defaultValue = "") String filter) {
        System.out.println("컨트롤러 안에 들어왔니?");
        Page<TbBoard> paging = this.tbBoardService.boardFilter(page, filter);
        System.out.println("서비스 갔다 왔니?"+paging);
        model.addAttribute("paging", paging.getContent());
        return paging;
    }

    // 게시글 생성하는 페이지로 이동
    @GetMapping("/create")
    public String boardCreate(TbBoardForm tbBoardForm) {
        return "board-write";
    }

    // 게시글 생성
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String boardCreate(@Valid TbBoardForm tbBoardForm, BindingResult bindingResult, Principal principal,
                              //@RequestPart("files") List<MultipartFile> files,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            System.out.println("에러");
            return "board-write";
        }
        TbUser user = this.tbUserService.getUser(principal.getName());

        try {
            this.tbBoardService.boardCreate(tbBoardForm.getBdTitle(), tbBoardForm.getBdCategory(),
                    tbBoardForm.getBdContent(), user); // , files);
            System.out.println("서비스 넘어감");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "파일업로드에 실패하셨습니다.");
            System.out.println("파일업로드 실패");
            return "board-write";
        }
        System.out.println("잘됨");
        return "redirect:/board/main";
        // return "board-list2";
    }

    // 게시글 삭제
    @PostMapping("/delete/{bdIdx}")
    public String boardDelete(@PathVariable("bdIdx") Long bdIdx, Principal principal) {
        TbBoard tbBoard = this.tbBoardService.boardDetail(bdIdx);
        if(!tbBoard.getTbUser().getUserNick().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다.");
        }
        this.tbBoardService.boardDelete(bdIdx);
        return "redirect:/board/main";
    }

    // 게시글 상세보기
    @GetMapping("/detail/{bdIdx}")
    public String boardDetail(Model model, @PathVariable("bdIdx") Long bdIdx, Principal principal) {
        TbBoard tbBoard = this.tbBoardService.boardDetail(bdIdx);
        model.addAttribute("tbBoard", tbBoard);

        boolean isAuthor = false;
        if (principal != null) {
            String userName = principal.getName();
            isAuthor = tbBoard.getTbUser().getUserEmail().equals(userName);
        }
        List<TbComment> commentList = this.tbCommentService.getAllComments(bdIdx);
        model.addAttribute("commentList", commentList);
        model.addAttribute("isAuthor", isAuthor);
        return "board-view";
    }

    // 게시글 수정 페이지로 이동
    @GetMapping("/update/{bdIdx}")
    public String boardUpdate(TbBoardForm tbBoardForm, @PathVariable("bdIdx") Long bdIdx, Principal principal) {
        TbBoard tbBoard = tbBoardService.boardDetail(bdIdx);
        if(!tbBoard.getTbUser().getUserNick().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        tbBoardForm.setBdCategory(tbBoard.getBdCategory());
        tbBoardForm.setBdTitle(tbBoard.getBdTitle());
        tbBoardForm.setBdContent(tbBoard.getBdContent());
        return "board-update";
    }

    // 게시글 수정
    @PostMapping("/update/{bdIdx}")
    public String boardUpdate(@Valid TbBoardForm tbBoardForm, BindingResult bindingResult, Principal principal, @PathVariable("bdIdx") Long bdIdx ){
        if(bindingResult.hasErrors()) {
            return "board_form";
        }
        TbBoard tbBoard = this.tbBoardService.boardDetail(bdIdx);
        if(!tbBoard.getTbUser().getUserNick().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.tbBoardService.boardUpdate(tbBoard, tbBoardForm.getBdTitle(), tbBoardForm.getBdCategory(), tbBoardForm.getBdContent());
        return String.format("redirect:/board/detail/%s", bdIdx);
    }
    
    
 // 게시글 좋아요
 	@PreAuthorize("isAuthenticated()")
 	@GetMapping("/like")
 	@ResponseBody
 	public ResponseEntity<Integer> boardLike(Principal principal, @RequestParam("bdIdx") Long bdIdx) {
 		try {
 			TbBoard tbBoard = this.tbBoardService.boardDetail(bdIdx);
 			TbUser tbUser = this.tbUserService.getUser(principal.getName());

 			// 좋아요 토글 수행
 			boolean liked = this.tbBoardService.boardLike(tbBoard, tbUser);

 			// 좋아요 갯수 반환
 			return ResponseEntity.ok(tbBoard.getBdLikes().size());
 		} catch (Exception e) {
 			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(-1);
 		}
 	}
}







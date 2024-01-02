package com.soon.slt.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.soon.slt.exception;
import com.soon.slt.entity.TbBoard;
import com.soon.slt.entity.TbComment;
import com.soon.slt.entity.TbUser;
import com.soon.slt.form.TbCommentForm;
import com.soon.slt.repository.TbCommentRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TbCommentService {
	
	private final TbCommentRepository tbCommentRepository;
	
	/*
	 * public TbComment createComment(TbBoard tbBoard, String cmtContent, TbUser
	 * tbUser) { TbComment tbComment = new TbComment();
	 * tbComment.setCmtContent(cmtContent);
	 * tbComment.setCreatedAt(LocalDateTime.now()); tbComment.setTbBoard(tbBoard);
	 * tbComment.setTbUser(tbUser); this.tbCommentRepository.save(tbComment); return
	 * tbComment; }
	 */
	
	// 댓글 생성
	public TbComment addComment(TbBoard tbBoard, String string, TbUser tbUser){
		System.out.println("댓글 생성 컨트롤러");
		TbComment c = new TbComment();
		c.setTbBoard(tbBoard);
		c.setTbUser(tbUser);
		c.setCmtContent(string);
		c.setCreatedAt(LocalDateTime.now());
		return tbCommentRepository.save(c);
	}
	
	// 댓글 불러오기
	public List<TbComment> getAllComments(Long bdIdx) {
		return tbCommentRepository.findByboard(bdIdx);
	}
	
	// 댓글 아이디로 댓글 조회
	public TbComment getComment(Long cmtIdx) {
		Optional<TbComment> tbcomment = this.tbCommentRepository.findById(cmtIdx);
		if (tbcomment.isPresent()) {
			return tbcomment.get();
		} else {
			throw new exception("댓글이 없습니다.");
		}
	}
	
	// 댓글 수정
	public void commentUpdate(TbComment tbComment, String cmtContent) {
		tbComment.setCmtContent(cmtContent);
		tbComment.setCreatedAt(LocalDateTime.now());
		this.tbCommentRepository.save(tbComment);
	}
	
	// 댓글 삭제
	public void commentDelete(TbComment tbComment) {
		this.tbCommentRepository.delete(tbComment);
	}

	// 댓글 좋아요
	public void commentGood(TbComment tbComment, TbUser tbUser) {
		tbComment.getCmtLikes().add(tbUser);
		this.tbCommentRepository.save(tbComment);
	}
	
	
	
}

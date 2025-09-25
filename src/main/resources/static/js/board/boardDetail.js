$(document).ready(function() {
    // URL에서 boardId 추출
    const pathParts = window.location.pathname.split("/"); // e.g., /board/1
    const boardId = pathParts[pathParts.length - 1];

    // 게시글 상세 조회
    fetchBoardDetail(boardId);
    fetchComments(boardId);
});

// 게시글 상세 조회 호출
function fetchBoardDetail(boardId) {
    $.ajax({
        url: `/api/v1/boards/${boardId}`,
        type: "GET"
    }).done(function(response) {
        renderBoardDetail(response.data);
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
}

// 게시글 상세 조회 렌더링
function renderBoardDetail(board) {
    const container = $("#board-detail");

    const typeBadge = board.boardType === 'NOTICE' ? 'danger' : 'primary';
    const typeText = board.boardType === 'NOTICE' ? '공지사항' : '건의/문의';

    let imageHtml = '';

    if (board.files && board.files.length > 0) {
        const images = board.files
            .map(f => f.filePath)
            .filter(path => /\.(jpg|jpeg|png|gif|webp)$/i.test(path));

        if (images.length > 0) {
            imageHtml = `
            <div class="mt-3">
                <strong>이미지 첨부:</strong>
                <div class="d-flex flex-wrap">
                    ${images.map(p => `<img src="${p}" class="img-thumbnail me-2 mb-2" style="max-width:150px;">`).join('')}
                </div>
            </div>
        `;
        }
    }

    const updateBtnHtml = board.isOwner
        ? `<button id="update-board-btn" class="btn btn-primary ms-2" data-board-id="${board.boardId}">글 수정</button>`
        : '';
    
    // 삭제 버튼 조건부 생성
    const deleteBtnHtml = board.isOwner
        ? `<button id="delete-board-btn" class="btn btn-danger ms-2" data-board-id="${board.boardId}">글 삭제</button>`
        : '';

    const html = `
        <div id="board-detail-content" data-board-id="${board.boardId}" class="card mb-3">
            <div class="card-header">
                <span class="badge bg-${typeBadge} me-2">${typeText}</span>
                ${board.title}
            </div>
            <div class="card-body">
                <p>${board.content}</p>
                ${imageHtml}
            </div>
            <div class="card-footer text-muted">
                작성일: ${new Date(board.createdAt).toLocaleString()}
            </div>
        </div>
        <a href="/board" class="btn btn-secondary">목록 이동</a>
        ${updateBtnHtml}
        ${deleteBtnHtml}
    `;

    container.html(html);
}

// 게시글 수정
$(document).on("click", "#update-board-btn", function() {
    const boardId = $(this).data("board-id");

    if (confirm("글 수정 화면 이동 하시겠습니까?")) {
        window.location.href = `/board/${boardId}/update`;
    }
});

// 게시글 삭제 버튼
$(document).on("click", "#delete-board-btn", function() {
    const boardId = $(this).data("board-id");

    if (confirm("정말 삭제하시겠습니까?")) {
        deleteBoard(boardId);
    }
});

// 게시글 삭제 api 호출
function deleteBoard(boardId) {
    $.ajax({
        url: `/api/v1/boards/${boardId}`,
        type: "DELETE"
    }).done(() => {
        alert("게시글이 삭제되었습니다.");
        window.location.href = "/board";
    }).fail(handleServerError);
}

// 댓글 작성 api 호출
$(document).on("click", "#submit-comment", function() {
    const boardId = $("#board-detail-content").data("board-id");
    const content = $("#comment-content").val().trim();
    if (!content) {
        alert("댓글 내용을 입력해주세요.");
        return;
    }

    $.ajax({
        url: "/api/v1/comments",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            boardId: boardId,
            content: content
        })
    }).done(function(response) {
        $("#comment-content").val("");
        showSnackbar(response.message);
        fetchComments(boardId); // 등록 후 다시 목록 조회
    }).fail(handleServerError);
});

// 댓글 조회 api 호출
function fetchComments(boardId) {
    $.ajax({
        url: `/api/v1/comments?boardId=${boardId}&page=1&size=10`, // 페이지네이션 필요시
        type: "GET"
    }).done(function(response) {
        renderCommentList(response.data.content); // PageResponseDto.content
    }).fail(function(xhr) {
        handleServerError(xhr);
    });
}

// 댓글 + 답글 재귀 렌더링
function renderCommentList(comments, container = $("#comment-list"), level = 0) {
    if (!comments || comments.length === 0) {
        if (level === 0) container.html("<p class='text-muted'>등록된 댓글이 없습니다.</p>");
        return;
    }

    let html = level === 0 ? `<ul class="list-group">` : `<ul class="list-group ms-${level * 3}">`;

    comments.forEach(c => {
        console.log(c);
        html += `
          <li class="list-group-item mt-2" data-comment-id="${c.commentId}">
            <strong>${c.writerName}</strong> 
            <small class="text-muted">
                ${new Date(c.createdAt).toLocaleString()}
                ${c.isOwner ? `
                    <button class="btn btn-sm btn-outline-danger delete-comment">삭제</button>
                ` : ''}
            </small>
            <p>${c.content}</p>
            
            <button class="btn btn-sm btn-link reply-toggle">답글 달기</button>
            
            <div class="reply-section mt-2" style="display:none;">
                <textarea class="form-control mb-1 reply-content" rows="2" placeholder="답글을 입력하세요."></textarea>
                <button class="btn btn-sm btn-secondary submit-reply">등록</button>
            </div>
        `;

        if (c.children && c.children.length > 0) {
            html += renderCommentList(c.children, null, level + 1); // 재귀 호출
        }

        html += `</li>`;
    });

    html += `</ul>`;

    if (level === 0) container.html(html); // 최상위 호출에서만 container에 반영
    else return html; // 재귀 호출에서는 html 반환
}

// 답글 입력창 토글
$(document).on("click", ".reply-toggle", function() {
    const replySection = $(this).siblings(".reply-section");
    replySection.toggle();
});

// 답글 등록
$(document).on("click", ".submit-reply", function() {
    const commentLi = $(this).closest("li[data-comment-id]");
    const commentId = commentLi.data("comment-id");
    const boardId = $("#board-detail-content").data("board-id");
    const content = commentLi.find(".reply-content").val().trim();

    if (!content) {
        alert("답글 내용을 입력해주세요.");
        return;
    }

    $.ajax({
        url: "/api/v1/comments", // 필요시 /reply로 분리
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            boardId: boardId,
            parentCommentId: commentId,
            content: content
        })
    }).done(function(response) {
        commentLi.find(".reply-content").val(""); // 입력창 초기화
        commentLi.find(".reply-section").hide(); // 입력창 숨김
        showSnackbar(response.message);
        fetchComments(boardId); // 등록 후 전체 댓글/답글 다시 조회
    }).fail(handleServerError);
});

// 댓글 삭제 버튼 클릭
$(document).on("click", ".delete-comment", function() {
    const commentLi = $(this).closest("li[data-comment-id]");
    const commentId = commentLi.data("comment-id");
    const boardId = $("#board-detail-content").data("board-id");

    if (confirm("정말 댓글을 삭제하시겠습니까?")) {
        $.ajax({
            url: `/api/v1/comments/${commentId}`, // 논리 삭제 API 엔드포인트
            type: "DELETE"
        }).done(function(response) {
            showSnackbar(response.message || "댓글이 삭제되었습니다.");
            fetchComments(boardId); // 삭제 후 전체 댓글 다시 조회
        }).fail(handleServerError);
    }
});
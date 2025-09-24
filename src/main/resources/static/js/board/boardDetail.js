$(document).ready(function() {
    // URL에서 boardId 추출
    const pathParts = window.location.pathname.split("/"); // e.g., /board/1
    const boardId = pathParts[pathParts.length - 1];

    // 게시글 상세 조회
    fetchBoardDetail(boardId);
});

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

function renderBoardDetail(board) {
    const container = $("#board-detail");

    const typeBadge = board.boardType === 'NOTICE' ? 'danger' : 'primary';
    const typeText = board.boardType === 'NOTICE' ? '공지사항' : '건의/문의';

    let imageHtml = '';

    if (board.files && board.files.length > 0) {
        const images = board.files
            .map(f => f.filePath)
            .filter(path => /\.(jpg|jpeg|png|gif)$/i.test(path));

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
        <div class="card mb-3">
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

$(document).on("click", "#update-board-btn", function() {
    const boardId = $(this).data("board-id");

    if (confirm("글 수정 화면 이동 하시겠습니까?")) {
        window.location.href = `/board/${boardId}/update`;
    }
});

$(document).on("click", "#delete-board-btn", function() {
    const boardId = $(this).data("board-id");

    if (confirm("정말 삭제하시겠습니까?")) {
        deleteBoard(boardId);
    }
});

function deleteBoard(boardId) {
    $.ajax({
        url: `/api/v1/boards/${boardId}`,
        type: "DELETE"
    }).done(() => {
        alert("게시글이 삭제되었습니다.");
        window.location.href = "/board";
    }).fail(handleServerError);
}
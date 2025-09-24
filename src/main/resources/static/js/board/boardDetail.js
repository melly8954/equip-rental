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

    let fileHtml = '';
    let imageHtml = '';

    if (board.filePath && board.filePath.length > 0) {
        const images = board.filePath.filter(f => /\.(jpg|jpeg|png|gif)$/i.test(f));

        if (images.length > 0) {
            imageHtml = `
                <div class="mt-3">
                    <strong>이미지 첨부:</strong>
                    <div class="d-flex flex-wrap">
                        ${images.map(f => `<img src="${f}" class="img-thumbnail me-2 mb-2" style="max-width:150px;">`).join('')}
                    </div>
                </div>
            `;
        }
    }

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
        <a href="/board" class="btn btn-secondary">목록으로</a>
    `;

    container.html(html);
}
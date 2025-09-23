let filters = {}; // 기본적으로 빈 객체

$(document).ready(function () {
    fetchBoardList(); // 초기 로드
});

function fetchBoardList() {
    $.ajax({
        url: "/api/v1/boards",
        type: "GET",
        data: filters,
    }).done(function(response) {
        renderBoardList(response.data.content); // 리스트
        renderPagination("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchBoardList({ ...filters, page: newPage });
        });
    }).fail(handleServerError);
}

// 게시글 리스트 렌더링
function renderBoardList(list) {
    const noticeContainer = $("#notice-list");
    const boardContainer = $("#board-list");

    noticeContainer.empty();
    boardContainer.empty();

    if (!list || list.length === 0) {
        boardContainer.append(`<div class="text-center py-3">등록된 게시글이 없습니다.</div>`);
        return;
    }

    list.forEach(board => {
        const isNotice = board.boardType === "NOTICE";
        const type = isNotice ? "공지사항" : "건의/문의";

        const row = $(`
            <div class="d-flex py-2 fw-bold">
                <div class="col-1 text-center">${board.boardId}</div>
                <div class="col-1 text-center me-3" style="${isNotice ? 'border: 1px solid rgba(255,0,0,0.5); border-radius: 4px; color: red;' : ''}">
                    ${type}
                </div>
                <div class="col-5" style="${isNotice ? 'color: red;' : ''}">
                    <a href="/board/${board.boardId}" style="${isNotice ? 'color: red;' : ''}">${board.title}</a>
                </div>
                <div class="col-2 text-center">${board.writerName}</div>
                <div class="col-3 text-center">
                    ${new Date(board.createdAt).toLocaleDateString()}
                </div>
            </div>
        `);

        if (isNotice) {
            noticeContainer.append(row);
        } else {
            boardContainer.append(row);
        }
    });
}
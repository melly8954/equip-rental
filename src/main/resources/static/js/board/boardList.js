let filters = {}; // 기본 필터
let onlyNotices = false; // 공지사항 전용 모드 여부

$(document).ready(function () {
    $("#search-keyword").on("input", function() {
        const searchType = $("#search-type").val();
        const keyword = $(this).val();

        filters.searchType = searchType;
        filters.keyword = keyword;

        if (onlyNotices) {
            fetchNoticeBoards(filters);  // 공지 모드일 땐 여기로
        } else {
            fetchBoardList(filters);     // 일반 모드일 땐 여기로
        }
    });

    loadBoardPage(); // 초기 로드

    // 버튼 이벤트
    $("#toggleNoticeBtn").click(function () {
        onlyNotices = !onlyNotices; // 모드 토글
        if (onlyNotices) {
            $(this).text("전체 글 보기");
        } else {
            $(this).text("공지사항만 보기");
        }
        loadBoardPage();
    });
});

// 초기 로드
function loadBoardPage() {
    if (onlyNotices) {
        // 공지사항만 보기 모드
        fetchNoticeBoards();
    } else {
        // 1) 공지 5개
        fetchNotices();
        // 2) 일반글 10개
        fetchBoardList(filters);
    }
}

// 공지 5개 조회
function fetchNotices() {
    $.ajax({
        url: "/api/v1/boards/notices",
        type: "GET"
    }).done(function (response) {
        renderNoticeList(response.data);
    }).fail(handleServerError);
}

// 일반글 10개 조회
function fetchBoardList(params = {}) {
    $.ajax({
        url: "/api/v1/boards",
        type: "GET",
        data: {
            ...params,
            boardType: "SUGGESTION"
        }
    }).done(function (response) {
        renderBoardList(response.data.content);
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

// 공지사항만 보기 (10개 페이징)
function fetchNoticeBoards(params = {}) {
    $.ajax({
        url: "/api/v1/boards",
        type: "GET",
        data: {
            ...filters,
            boardType: "NOTICE"
        }
    }).done(function (response) {
        $("#notice-list").empty(); // 상단 공지 영역 제거
        renderBoardList(response.data.content);
        renderPagination("pagination", {
            page: response.data.page,
            totalPages: response.data.totalPages,
            first: response.data.first,
            last: response.data.last
        }, (newPage) => {
            fetchNoticeBoards({ page: newPage });
        });
    }).fail(handleServerError);
}

// 공지글 전용 렌더링
function renderNoticeList(list) {
    const noticeContainer = $("#notice-list");
    noticeContainer.empty();

    if (!list || list.length === 0) {
        noticeContainer.append(`<div class="text-center py-2">공지글이 없습니다.</div>`);
        return;
    }

    list.forEach(board => {
        const row = renderRow(board, true);
        noticeContainer.append(row);
    });
}

// 공지 + 일반글 렌더링 공통 함수
function renderBoardList(list) {
    const boardContainer = $("#board-list");
    boardContainer.empty();

    if (!list || list.length === 0) {
        boardContainer.append(`<div class="text-center py-3">등록된 게시글이 없습니다.</div>`);
        return;
    }

    list.forEach(board => {
        const row = renderRow(board, board.boardType === "NOTICE");
        boardContainer.append(row);
    });
}

// 행 렌더링
function renderRow(board, isNotice) {
    const type = isNotice ? "공지사항" : "건의/문의";
    return $(`
        <div class="d-flex py-2 fw-bold">
            <div class="col-1 text-center">${board.boardId}</div>
            <div class="col-1 text-center me-3" style="${isNotice ? 'border: 1px solid rgba(255,0,0,0.5); border-radius: 4px; color: red;' : ''}">
                ${type}
            </div>
            <div class="col-5" style="${isNotice ? 'color: red;' : ''}">
                <a href="/board/${board.boardId}" style="${isNotice ? 'color: red;' : ''}">${board.title}</a>
            </div>
            <div class="col-2 text-center">${board.writerName}</div>
            <div class="col-3 text-center">${new Date(board.createdAt).toLocaleDateString()}</div>
        </div>
    `);
}

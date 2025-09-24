let deletedFileIds = []; // 삭제 요청할 파일 id 저장

$(document).ready(function() {
    const pathParts = window.location.pathname.split("/");
    const boardId = pathParts[pathParts.length - 2]; // /board/{id}/update →倒수 2번째가 id

    fetchBoardDetail(boardId);

    $("#submitBtn").on("click", function() {
        updateBoard(boardId);
    });
});

function fetchBoardDetail(boardId) {
    $.ajax({
        url: `/api/v1/boards/${boardId}`,
        type: "GET"
    }).done(function(response) {
        const board = response.data;
        // 폼 채우기
        $("#boardType").val(board.boardType);
        $("#title").val(board.title);
        $("#content").val(board.content);

        // 기존 파일 미리보기
        const $previewList = $('#filePreviewList');
        $previewList.empty();
        board.files.forEach(file => {
            const listItem = $('<li>')
                .addClass('list-group-item d-flex justify-content-between align-items-center')
                .text(file.originalName);

            const removeBtn = $('<button>')
                .addClass('btn btn-sm btn-danger')
                .text('삭제')
                .on('click', function() {
                    deletedFileIds.push(file.id); // 삭제 목록에 추가
                    listItem.remove();
                });

            listItem.append(removeBtn);
            $previewList.append(listItem);
        });
    }).fail(handleServerError);
}

// 저장 시
function updateBoard(boardId) {
    const boardType = $('#boardType').val();
    const title = $('#title').val();
    const content = $('#content').val();
    const files = $('#files')[0].files;

    if (!boardType || !title || !content) {
        showSnackbar('모든 필드를 입력해주세요.');
        return;
    }

    // FormData 객체 생성
    const formData = new FormData();
    const data = JSON.stringify({ boardType, title, content, deletedFileIds });

    formData.append('data', new Blob([data], { type: "application/json" }));

    for (let i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
    }

    $.ajax({
        url: `/api/v1/boards/${boardId}`,
        type: "PATCH",
        processData: false,
        contentType: false,
        data: formData
    }).done(function() {
        alert("게시글이 수정되었습니다.");
        window.location.href = `/board/${boardId}`;
    }).fail(handleServerError);
}

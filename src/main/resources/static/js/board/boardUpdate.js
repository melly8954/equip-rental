let deletedFileIds = []; // 삭제 요청할 파일 id 저장

$(document).ready(function() {
    const pathParts = window.location.pathname.split("/");
    const boardId = pathParts[pathParts.length - 2]; // /board/{id}/update →倒수 2번째가 id

    fetchBoardDetail(boardId);

    // 새 파일 선택 시 미리보기 추가
    $('#files').on('change', function() {
        const files = Array.from(this.files);
        const $previewList = $('#filePreviewList');

        files.forEach(file => {
            // 리스트 아이템 생성
            const listItem = $('<li>')
                .addClass('list-group-item d-flex justify-content-between align-items-center');

            // 파일 이름
            const fileNameSpan = $('<span>').text(file.name);

            // "신규" 배지
            const newBadge = $('<span>')
                .addClass('badge bg-success ms-2') // 초록색, margin-left
                .css('font-size', '0.75rem')      // 작게
                .text('신규');

            fileNameSpan.append(newBadge);

            // 삭제 버튼
            const removeBtn = $('<button>')
                .addClass('btn btn-sm btn-danger')
                .text('삭제')
                .on('click', function() {
                    const dt = new DataTransfer();
                    Array.from($('#files')[0].files)
                        .filter(f => f.name !== file.name)
                        .forEach(f => dt.items.add(f));
                    $('#files')[0].files = dt.files;

                    listItem.remove();
                });

            listItem.append(fileNameSpan);
            listItem.append(removeBtn);
            $previewList.append(listItem);
        });
    });


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
                    deletedFileIds.push(file.fileId); // 삭제 목록에 추가
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

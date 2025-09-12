function handleServerError(jqXHR) {
    console.log(jqXHR);
    // 서버에서 내려준 메시지 활용 (JSON 응답인 경우)
    if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
        alert(jqXHR.responseJSON.message);
    }
}

function renderFilter(containerId, config, onChange) {
    const container = $("#" + containerId);
    container.empty(); // 초기화

    $.each(config, function(key, value) {
        const group = $("<div>").addClass("mb-3");

        const label = $("<label>").addClass("form-label").text(value.label);
        group.append(label);

        const btnGroup = $("<div>")
            .addClass("btn-group w-100")
            .attr("role", "group");

        $.each(value.options, function(_, opt) {
            const inputId = key + "-" + opt;
            const input = $("<input>")
                .attr("type", value.type)
                .addClass("btn-check")
                .attr("name", key)
                .attr("id", inputId)
                .val(opt === "전체" ? "" : opt);

            // "전체"는 항상 기본 checked
            if (opt === "전체") {
                input.prop("checked", true);
            }

            // UI에 표시할 label
            let displayText = opt;
            if (key === "category") {
                displayText = categoryLabelMap[opt] || opt;
            } else if (key === "status") {
                displayText = statusLabelMap?.[opt] || opt;
            }

            const button = $("<label>")
                .addClass("btn btn-outline-primary")
                .attr("for", inputId)
                .text(displayText );

            input.on("change", function() {
                onChange(getFilterValues(config));
            });

            btnGroup.append(input, button);
        });

        group.append(btnGroup);
        container.append(group);
    });
}

function getFilterValues(config) {
    // values 라는 빈 객체 생성
    const values = {};
    $.each(config, function(key) {
        const selected = $(`input[name="${key}"]:checked`);
        values[key] = selected.length ? selected.val() : "";
    });
    // 빈 객체에 config key/value 담아서 반환
    return values;
}

// 페이징 렌더링
function renderPagination(containerId, pageInfo, onPageChange) {
    const container = $("#" + containerId);
    container.empty();

    // if (pageInfo.totalPages <= 1) return; // 한 페이지면 페이징 필요 없음

    const pagination = $('<ul class="pagination justify-content-center"></ul>');

    // 이전 페이지
    const prevLi = $('<li class="page-item"></li>');
    const prevLink = $('<a class="page-link" href="#">이전</a>');
    if (pageInfo.first) prevLi.addClass("disabled");
    prevLink.on("click", (e) => {
        e.preventDefault();
        if (!pageInfo.first) onPageChange(pageInfo.page - 1);
    });
    prevLi.append(prevLink);
    pagination.append(prevLi);

    // 페이지 번호
    for (let i = 1; i <= pageInfo.totalPages; i++) {
        const li = $('<li class="page-item"></li>');
        if (i === pageInfo.page) li.addClass("active");
        const link = $(`<a class="page-link" href="#">${i}</a>`);
        link.on("click", (e) => {
            e.preventDefault();
            if (i !== pageInfo.page) onPageChange(i);
        });
        li.append(link);
        pagination.append(li);
    }

    // 다음 페이지
    const nextLi = $('<li class="page-item"></li>');
    const nextLink = $('<a class="page-link" href="#">다음</a>');
    if (pageInfo.last) nextLi.addClass("disabled");
    nextLink.on("click", (e) => {
        e.preventDefault();
        if (!pageInfo.last) onPageChange(pageInfo.page + 1);
    });
    nextLi.append(nextLink);
    pagination.append(nextLi);

    container.append(pagination);
}

function getCurrentPage(containerId) {
    const activeLink = $(`#${containerId} .page-item.active a`);
    if (activeLink.length) {
        return parseInt(activeLink.text(), 10);
    }
    return 1; // 기본 1페이지
}
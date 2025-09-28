$(document).ready(function() {
    fetchKpiData();

    // 이벤트 위임: 카드 내 상세보기 버튼 클릭
    $('#kpi-cards').on('click', '.kpi-detail-label', function() {
        const kpiName = $(this).closest('.card-body').find('.card-title').text();

        let params = new URLSearchParams();
        let targetUrl = '';

        if (kpiName.includes('연체')) {
            params.set("rentalItemStatus", "OVERDUE");
            targetUrl = '/admin/rental/item/list';
        } else if (kpiName.includes('대기')) {
            // 승인 대기 건수는 다른 페이지로 이동
            targetUrl = '/admin/rental/list';
        }

        if (targetUrl) {
            window.location.href = targetUrl + (params.toString() ? '?' + params.toString() : '');
        }
    });
});

// KPI 데이터 가져오기
function fetchKpiData() {
    $.ajax({
        url: '/api/v1/dashboards/kpi',
        method: 'GET',
    }).done(function(response) {
        renderKpiCards(response.data.kpis);
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    })
}

// KPI 카드 렌더링
function renderKpiCards(kpis) {
    const container = $('#kpi-cards');
    container.empty();

    kpis.forEach(kpi => {
        let changeHtml = '';
        if (kpi.changeRate) {
            if (kpi.changeRate === '신규 발생') {
                changeHtml = `<small class="text-primary fw-bold">(${kpi.changeRate})</small>`;
            } else {
                const rate = parseFloat(kpi.changeRate.replace('%', ''));
                const color = rate > 0 ? 'text-success' : 'text-danger';
                const arrow = rate > 0 ? '↑' : '↓';
                changeHtml = `<small class="${color} fw-bold">(${arrow} ${kpi.changeRate})</small>`;
            }
        }

        const cardHtml = `
            <div style="flex: 1 1 18%; min-width: 150px;">
                <div class="card text-center shadow-sm h-100">
                    <div class="card-body d-flex flex-column justify-content-center">
                        <h5 class="card-title">${kpi.name}</h5>
                        <p class="card-text fs-3 fw-bold mb-1">${kpi.value}</p>
                        ${changeHtml}
                        ${kpi.name.includes('연체') || kpi.name.includes('대기')
                            ? `<span class="kpi-detail-label">(목록 이동)</span>`
                            : ''}
                    </div>
                </div>
            </div>
        `;

        container.append(cardHtml);
    });
}


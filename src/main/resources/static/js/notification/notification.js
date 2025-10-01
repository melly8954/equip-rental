const badge = $('#unread-count-badge');

$(document).ready(function() {
    // 초기 조회
    fetchUnreadCount();

    // 주기적 갱신 (30초마다)
    setInterval(fetchUnreadCount, 30000);
});

function fetchUnreadCount() {
    const unreadText = $('#unread-count-text');

    $.ajax({
        url: '/api/v1/notifications/unread-count',
        method: 'GET',
    }).done(function(response) {
        const count = response.data.unreadCount || 0;
        updateUnreadCount(count);
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    })
}

function updateUnreadCount(count) {
    if (count > 0) {
        badge.text(count > 99 ? '99+' : count);
        badge.show();
    } else {
        badge.hide();
    }
}
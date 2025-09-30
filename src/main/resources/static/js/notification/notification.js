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
        unreadText.text('읽지 않은 알림 ' + count + '개');
        unreadText.data('count', count);
    }).fail(function(jqXHR) {
        handleServerError(jqXHR);
    })
}
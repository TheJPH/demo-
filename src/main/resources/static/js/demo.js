function post() {
    var questionId = $("#question_id").val();
    var content = $("#comment_comment").val();
    $.ajax({
        type: "POST",
        url: "/comment",
        contentType: 'application/json',
        data: JSON.stringify({
            "parentId": questionId,
            "content": content,
            "type": 1
        }),
        success: function (response) {
            if (response.code == 200) {
                $("#comment_section").hide();
            } else {
                if (response.code == 2003) {
                    var isAccepted = confirm(response.messae);
                    if (isAccepted) {
                        window.open("https://github.com/login/oauth/authorize?client_id=de22b2d47b8a1b812980&redirect_uri=http://localhost:8088/callback&scope=user&state=1");
                        window.localStorage.setItem("closable", true);
                    }
                } else {
                    alert(response.message);
                }
            }
        },
        dataType: "json"
    });
}

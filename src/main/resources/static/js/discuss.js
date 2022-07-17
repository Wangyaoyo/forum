 function like(btn, entityType, entityId, entityUserId) {
    // post请求
    $.post(
        "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data) {
            data = $.parseJSON(data);
            console.log(data);
            if(data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
            } else {
                alert(data.msg);
            }
        }
    );
}

 $(function(){
     $("#topBtn").click(setTop);
     $("#wonderfulBtn").click(setWonderful);
     $("#deleteBtn").click(setDelete);
 });

 // 置顶
 function setTop() {
     $.post(
         "/top",
         {"id":$("#postId").val()},
         function(data) {
             data = $.parseJSON(data);
             if(data.code == 0) {
                 $("#topBtn").attr("disabled", "disabled");
             } else {
                 alert(data.msg);
             }
         }
     );
 }

 // 加精
 function setWonderful() {
     $.post(
         "/wonderful",
         {"id":$("#postId").val()},
         function(data) {
             data = $.parseJSON(data);
             if(data.code == 0) {
                 $("#wonderfulBtn").attr("disabled", "disabled");
             } else {
                 alert(data.msg);
             }
         }
     );
 }

 // 删除
 function setDelete() {
     $.post(
         "/delete",
         {"id":$("#postId").val()},
         function(data) {
             data = $.parseJSON(data);
             if(data.code == 0) {
                 location.href = "/index";
             } else {
                 alert(data.msg);
             }
         }
     );
 }
$(function() {
   var bar = $('.progress-bar');
   var percent = $('.perceentage');
   var status = $('#status');
   $('form').ajaxFrom({
       beforeSend: function() {
           status.empty();
           var percentVal = '0%';
           bar.width(percentVal);
           percent.html(percentVal);
       },
       complete: function(xhr) {
           alert('성공;');
       },
       error: function(e) {
           alert('실패');
       }
   })
});
$ = jQuery;
$(document).ready(function(){
    $(".button-collapse").sideNav();
    $('.modal-trigger').leanModal();
    $('input.counted, textarea.counted').characterCounter();
});
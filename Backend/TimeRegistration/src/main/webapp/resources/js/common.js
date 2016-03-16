$ = jQuery;
$(document).ready(function(){
    $(".button-collapse").sideNav();
    $('.modal-trigger').leanModal();
    $('input.counted, textarea.counted').characterCounter();
});

window.validate_field = function(e) {}
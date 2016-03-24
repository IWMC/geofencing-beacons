$ = jQuery;
$(document).ready(function () {
    $(".button-collapse").sideNav();
    $('.modal-trigger').leanModal();
    $('input.counted, textarea.counted').characterCounter();
    $('.datepicker').pickadate({
        selectMonths: true,
        selectYears: 15
    });

    $('.label-datepicker').each(function () {
        var l = $(this);
        var datepicker = $('.datepicker', l.parent());
        var value = l.text();
        datepicker.attr('placeholder', value);
        l.remove();
    });
});

window.validate_field = function (e) {
}
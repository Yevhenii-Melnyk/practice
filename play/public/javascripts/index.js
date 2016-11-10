$(document).ready(function () {
    $.get("/people", function (people) {
        people.forEach(function (item) {
            $("#people").append($("<li>").text(item.name));
        });
    });
});
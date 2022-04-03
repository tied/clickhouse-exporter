(function ($) { // this closure helps us keep our variables to ourselves.
// This pattern is known as an "iife" - immediately invoked function expression

    // form the URL
    var url = AJS.contextPath() + "/rest/clickhouse-exporter/1.0/";

    // wait for the DOM (i.e., document "skeleton") to load.
    $(function() {
        // request the config information from the server
        $.ajax({
            url: url,
            dataType: "json"
        }).done(function(config) { // when the configuration is returned...
            // ...populate the form.
            $("#name").val(config.name);
            $("#time").val(config.time);
        });

        function updateConfig() {
            $.ajax({
                url: url,
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify({ name: $("#name").val(), time: $("#time").val() }),
                processData: false
            });
        }

        $("#admin").on("submit", function(e) {
            e.preventDefault();
            updateConfig();
        });
    });
})(AJS.$ || jQuery);
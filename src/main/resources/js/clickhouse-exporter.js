(function ($) { // this closure helps us keep our variables to ourselves.
// This pattern is known as an "iife" - immediately invoked function expression

    // form the URL
    const url = AJS.contextPath() + "/rest/clickhouse-exporter/1.0/";

    // wait for the DOM (i.e., document "skeleton") to load.
    $(function () {
        // request the config information from the server
        $.ajax({
            url: url,
            dataType: "json"
        }).done(function (config) { // when the configuration is returned...
            // ...populate the form.
            $("#jdbc_url").val(config.jdbc_url);
            $("#project_code").val(config.project_code);
            $("input[name='issue_types']").map((i, e) => e.checked = config.issue_types.includes(e.value));
        });

        function updateConfig() {
            $.ajax({
                url: url,
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify({
                    jdbc_url: $("#jdbc_url").val(),
                    project_code: $("#project_code").val(),
                    issue_types: $("input[name='issue_types']:checked").map((i, e) => e.value).get()
                }),
                processData: false
            });
        }

        $("#admin").on("submit", function (e) {
            e.preventDefault();
            updateConfig();
        });
    });
})(AJS.$ || jQuery);
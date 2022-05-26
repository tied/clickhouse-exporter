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
            $("#events_table").val(config.events_table);
            $("#project_code").val(config.project_code);
            $("#old_date").val(config.old_date);
            $("input[name='issue_types']").map((i, e) =>  e.checked = config.issue_types.includes(e.value));
            $("input[name='issue_fields']").map((i, e) => !e.disabled && (e.checked = config.issue_fields.includes(e.value)));
        });

        function updateConfig() {
            $.ajax({
                url: url,
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify({
                    jdbc_url: $("#jdbc_url").val(),
                    events_table: $("#events_table").val(),
                    project_code: $("#project_code").val(),
                    old_date: $("#old_date").val(),
                    issue_types: $("input[name='issue_types']:checked").map((i, e) => e.value).get(),
                    issue_fields: $("input[name='issue_fields']:checked").map((i, e) => e.value).get()
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
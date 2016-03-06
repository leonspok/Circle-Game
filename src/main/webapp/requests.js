/**
 * Created by igorsavelev on 05/03/16.
 */
function sendRequest(url, method, params, success, failure) {
    var xhttp = new XMLHttpRequest();

    if (method == "GET") {
        url += "?"+params;
    }

    xhttp.open(method, url, true);
    if (params && method == "POST") {
        xhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhttp.setRequestHeader("Content-Length", params.length);
    }

    xhttp.onreadystatechange = function() {
        if (xhttp.readyState == 4) {
            if (xhttp.status == 200) {
                try {
                    if (xhttp.responseText) {
                        var response = JSON.parse(xhttp.responseText);
                        success(response);
                    } else {
                        success(null);
                    }
                } catch (e) {
                    failure();
                }
            } else {
                failure();
            }
        }
    }

    if (method == "GET") {
        xhttp.send();
    } else {
        xhttp.send(params);
    }
}
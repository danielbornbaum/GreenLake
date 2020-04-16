export function restRequest(url, method, requestBody, onCallback, onError){
    var request = new XMLHttpRequest();
    request.onreadystatechange = function(){
        if (this.readyState == 4 && this.status == 200) {
            onCallback(this.responseText);
        } else if (this.readyState == 4){ //status not 200
            onError(this.status, this.responseText);
        }
    }
    request.open(method, url, true);
    request.setRequestHeader("Content-type", "application/json");
    request.send(requestBody);
}
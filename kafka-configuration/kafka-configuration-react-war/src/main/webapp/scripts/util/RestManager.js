export function restRequest(url, method, requestBody, onCallback, onError){
    var request = new XMLHttpRequest();
    request.onreadystatechange = function(){
        if (this.readyState == 4 && this.status == 200) {
            onCallback(this.responseText);
        } else if (this.readyState == 4){ //status not 200
            if (onError !== undefined) onError();
            alert("Ein Fehler ist aufgetreten: "+this.status+"\n"+JSON.parse(this.responseText).message);
        }
    }
    request.open(method, url + ((/\?/).test(url) ? "&" : "?") + (new Date()).getTime(), true);
    request.setRequestHeader("Content-type", "application/json");
    request.send(requestBody);
}
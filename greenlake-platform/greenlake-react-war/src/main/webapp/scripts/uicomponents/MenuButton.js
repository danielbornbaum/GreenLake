import React from "react"
import "../../styles/uicomponents/menu-button.css"

export class MenuButton extends React.Component{

    constructor(){
        super();
    }

    buttonClickHandler(){
        var menubutton = document.getElementById("menubutton");

        if (menubutton.classList.contains("opened")){
            menubutton.classList.remove("opened");
            mainmenu.show()
        } else {
            menubutton.classList.add("opened");
            mainmenu.hide()
        }
    }

    render(){
        return(
            <div id="menubutton" onClick={this.buttonClickHandler}>
                <div className="menubutton-burger"></div>
            </div>
        )
    }
}
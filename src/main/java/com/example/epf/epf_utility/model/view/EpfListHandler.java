package com.example.epf.epf_utility.model.view;

import java.util.ArrayList;
import java.util.List;

public class EpfListHandler {
	private List<EpfViewHandler> handlerList;
	
	
	public EpfListHandler() {
		this.handlerList = new ArrayList<EpfViewHandler>();
	}
	
	public void pushAtStart(EpfViewHandler epfViewHandler) {
		this.handlerList.add(0,epfViewHandler);
	}
    
    public List<EpfViewHandler> getHandlerList() {
      return this.handlerList;
    }


}

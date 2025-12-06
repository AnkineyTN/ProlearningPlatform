package com.cabybara.prolearningplatform.event.model;


import com.cabybara.prolearningplatform.utils.SetChild;

public class ChildEntityUpdatedEvent {
    private final SetChild childEntity;

    public ChildEntityUpdatedEvent(SetChild childEntity) {
        this.childEntity = childEntity;
    }

    public SetChild getChildEntity() {
        return this.childEntity;
    }
}

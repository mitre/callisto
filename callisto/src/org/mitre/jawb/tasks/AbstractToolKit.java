package org.mitre.jawb.tasks;

import java.util.Set;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.ActionModel;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;

// SAM 1/30/06: Originally, every task implemented TaskToolKit, but
// then we realized that if we ever wanted to extend the default
// behavior of toolkits, we were screwed. So we changed every task
// to extend AbstractToolKit. However, at the moment, because we
// don't want to have to touch the task initialization code for
// each task, we're not doing things like, well, saving the task
// in the constructor for AbstractToolKit. One trauma at a time.

// The particular motivating trauma, at this point, was that we
// decided that the new ActionModels really needed to live in the toolkit,
// which meant we needed to extend the toolkit interface, and we
// really didn't want to have to put the same damn code in every
// task. So.

public abstract class AbstractToolKit implements TaskToolKit {

  public abstract Task getTask();

  public abstract JawbComponent getMainComponent();

  public abstract JawbComponent getEditorComponent();

  public abstract Set getActions();

  public abstract boolean deleteAnnotation(AWBAnnotation annot, JawbDocument doc);
  
  private ActionModel actionModel;
  
  public ActionModel getActionModel() {
    if (actionModel == null) {
      actionModel = new ActionModel(this);
    }
    return actionModel;
  }

  // to allow customization by context, must call this on the Toolkit
  // rather than the task -- this forwards to the task unless the
  // implementation wishes to customize
  public Set getExtentModifiableAnnotationTypes () {
    return getTask().getExtentModifiableAnnotationTypes();
  }
  
}

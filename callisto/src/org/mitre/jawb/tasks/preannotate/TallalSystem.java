package org.mitre.jawb.tasks.preannotate;

import org.mitre.jawb.tasks.*;

public class TallalSystem {
    private String name, desc;
    private Task task;
    private LP lp;
    private String dataOrURL;
    private String[] tags;

    public TallalSystem(String name) {
	this.name = name;
    }

    public String getName() { return name; }
    public String getDesc() { return desc; }
    public Task getTask() { return task; }
    public LP getLP() { return lp; }
    public String getDataOrURL() { return dataOrURL; }
    public String[] getTags() { return tags; }

    public void setDesc(String desc) { this.desc = desc; }
    public void setTask(Task task) { this.task = task; }
    public void setLP(LP lp) { this.lp = lp; }
    public void setDataOrURL(String dou) { this.dataOrURL = dou; }
    public void setTags(String[] tags) { this.tags = tags; }
}

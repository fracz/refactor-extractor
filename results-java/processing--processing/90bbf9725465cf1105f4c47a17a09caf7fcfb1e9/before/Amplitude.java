package processing.sound;
import processing.core.*;

public class Amplitude {

	PApplet parent;
	private MethClaInterface m_engine;
	private long ptr;

	public Amplitude(PApplet theParent) {
		this.parent = theParent;
		parent.registerMethod("dispose", this);
		m_engine = new MethClaInterface();
	}

	public void input(SoundObject input){
		ptr = m_engine.amplitude(input.returnId());
	}

	public float process(){
		return m_engine.poll_amplitude(ptr);
	}
	/*
	public void stop(){
		m_engine.synthStop(m_nodeId);
	}

	public int returnId(){
		return m_nodeId;
	}
	*/
	public void dispose() {
		m_engine.destroy_amplitude(ptr);
		//m_engine.synthStop(m_nodeId);
	}
}
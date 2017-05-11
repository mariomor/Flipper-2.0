package hmi.flipper2;

import java.util.ArrayList;
import java.util.List;

import hmi.flipper2.conditions.ConditionList;
import hmi.flipper2.conditions.JavaCondition;
import hmi.flipper2.conditions.JsCondition;
import hmi.flipper2.effect.AssignEffect;
import hmi.flipper2.effect.BehaviourJavaEffect;
import hmi.flipper2.effect.Effect;
import hmi.flipper2.effect.EffectList;
import hmi.flipper2.effect.FunctionJavaEffect;
import hmi.flipper2.effect.JavaEffect;
import hmi.flipper2.effect.MethodJavaEffect;
import hmi.flipper2.sax.SimpleElement;
import hmi.flipper2.value.ConstantJavaValue;
import hmi.flipper2.value.DbJavaValue;
import hmi.flipper2.value.IsJavaValue;
import hmi.flipper2.value.JavaValueList;

public class Template {

	public TemplateFile tf;
	
	public String id;
	public String name;

	public Template(TemplateFile tf, SimpleElement el) throws FlipperException {
		this.tf = tf;
		this.id = el.attr.get("id");
		this.name = el.attr.get("name");
		for (SimpleElement coe : el.children) {
			if (coe.tag.equals("preconditions")) {
				handle_preconditions(coe);
			} else if (coe.tag.equals("effects")) {
				handle_effects(coe);
			} else if (coe.tag.equals("javascript")) {
				this.tf.tc.is.execute(coe.characters.toString());
			} else
				throw new RuntimeException("INCOMPLETE: bad template: "+coe.tag);
		}
	}

	ConditionList preconditions = null;
	
	private void handle_preconditions(SimpleElement el) throws FlipperException {
		preconditions = new ConditionList(el.attr.get("mode"));
		for (SimpleElement pc : el.children) {
			if (pc.tag.equals("condition")) {
				preconditions.add(new JsCondition(pc.characters.toString()));
			} else if (pc.tag.equals("function") || pc.tag.equals("method")) {
				preconditions.add(new JavaCondition((JavaEffect)handle_effect(tf.tc.is, pc)));
			} else if (pc.tag.equals("javascript")) {
				this.tf.tc.is.execute(pc.characters.toString());
			} else
				throw new RuntimeException("INCOMPLETE: bad precondition: "+pc.tag);	
		}
	}

	List<EffectList> listOfEffectList = new ArrayList<EffectList>();
	
	private void handle_effects(SimpleElement el) throws FlipperException {
		String a_effect_mode = el.attr.get("mode");
		EffectList effects = new EffectList(( a_effect_mode != null && a_effect_mode.equals("weighted")) );
		listOfEffectList.add(effects);
		for (SimpleElement ee : el.children) {
			if ( ee.tag.equals("javascript"))
				this.tf.tc.is.execute(ee.characters.toString());
			else 
				effects.add(handle_effect(tf.tc.is, ee));
		}
	}
	
	public static Effect handle_effect(Is is, SimpleElement ee) throws FlipperException {
	    if (ee.tag.equals("assign")) {
			return new AssignEffect(ee.attr.get("is"), ee.characters.toString());
		} else if (ee.tag.equals("db")) {
			throw new RuntimeException("INCOMPLETE: DB ELEMENT: " + ee);
		} else if (ee.tag.equals("function") || ee.tag.equals("method") || ee.tag.equals("behaviour")) {
			String a_is = ee.attr.get("is");
			String a_is_type = ee.attr.get("is_type");
			String a_class = ee.attr.get("class");
			String a_name = ee.attr.get("name");
			String a_mode = ee.attr.get("mode");
			String a_weight = ee.attr.get("weight");
			JavaValueList arguments = null;
			JavaValueList constructors = null;
			for (SimpleElement lists : ee.children) {
				if (lists.tag.equals("arguments"))
					arguments = handle_value_list(is, lists);
				else if (lists.tag.equals("constructors"))
					constructors = handle_value_list(is, lists);
				else
					throw new FlipperException("INCOMPLETE: bad tag: " + lists.tag);
			}
			Effect newEffect = null;
			if (ee.tag.equals("function")) {
				newEffect = new FunctionJavaEffect(a_is, a_is_type, a_class, a_name, arguments);
			} else if (ee.tag.equals("method")) {
				newEffect = new MethodJavaEffect(a_is, a_is_type, a_class, constructors, a_name, arguments, a_mode);
			} else if (ee.tag.equals("behaviour")) {
				newEffect = new BehaviourJavaEffect(a_is, a_is_type, a_class, constructors, a_name, arguments, a_mode);
			}
			if (a_weight != null)
				newEffect.setWeight((new Double(a_weight)).doubleValue());
			return newEffect;
		} else
			throw new FlipperException("UNKNOWN effect: " + ee.tag);
	}
	
	private static JavaValueList handle_value_list(Is is, SimpleElement list) throws FlipperException {
		JavaValueList jvl = new JavaValueList();	
		for (SimpleElement val : list.children) {
			if (  val.attr.get("constant") != null )
				jvl.add( new ConstantJavaValue(val.attr.get("class"),val.attr.get("constant")));
			else if (  val.attr.get("is") != null ) {
				jvl.add( new IsJavaValue(is, val.attr.get("is"), val.attr.get("is_type"),val.attr.get("class")));
			} else if (  val.attr.get("db") != null ) {
				jvl.add( new DbJavaValue(is, val.attr.get("db")));
			} else 
				throw new FlipperException("INCOMPLETE: bad value tag: "+val.tag);
		}
		return jvl;
	}
	
	public boolean check(Is is) throws FlipperException {
		if ( preconditions.checkIt(is) ) {
			for(EffectList effects: listOfEffectList)
				effects.doIt(is);
			return true;
		}
		return false;
	}
}
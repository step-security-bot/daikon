import Operator from './operator';
import { EMPTY_VALUE } from './constants';

/**
 * Class representing the Empty operator.
 * Will be serialized as follows : (field1 is empty)
 */
export default class Empty extends Operator {
	static value = EMPTY_VALUE;

	static hasOperand = false;
}

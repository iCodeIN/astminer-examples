//Wrapping the example in a class to make it valid java code so that it can be parsed
class Dummy {
    /*
     Checks if a target integer is present in the list of integers.
    */
    public Boolean contains(Integer target, List<Integer> numbers) {
        for (Integer number : numbers) {
            if (number.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
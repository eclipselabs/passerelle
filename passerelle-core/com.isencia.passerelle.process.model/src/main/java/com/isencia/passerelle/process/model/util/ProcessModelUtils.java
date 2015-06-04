package com.isencia.passerelle.process.model.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.Task;

public class ProcessModelUtils {

  /**
   * @param context
   * @param itemName
   *          can not be blank
   * @param defaultValue
   * @return the value of the context item with the given itemName, or the defaultValue if no such item was found.
   */
  public static String getContextItemValue(Context context, String itemName, String defaultValue) {
    if (context == null || StringUtils.isBlank(itemName)) {
      return defaultValue;
    } else {
      String itemValue = context.lookupValue(itemName);
      return itemValue != null ? itemValue : defaultValue;
    }
  }

  /**
   * Look for the item with given itemName in the results of given dataType in the previous tasks. The search is done
   * backwards from the last (i.e. most recent) one to the beginning, for the maximum depth given.
   * 
   * @param context
   *          can not be null
   * @param dataType
   *          if null or blank, look in all results otherwise only in results of the given type
   * @param itemName
   *          can not be blank
   * @param maxDepth
   *          if <= 0 all tasks are checked in backwards order; otherwise maximally check this nr of tasks in backwards
   *          order
   * @return
   */
  public static String lookupValueForMaxDepth(Context context, String dataType, String itemName, int maxDepth) {
    if (context == null || StringUtils.isBlank(itemName)) {
      return null;
    } else {
      String itemValue = null;
      if (maxDepth > 0) {
        maxDepth = maxDepth + 1;
        List<Task> tasks = context.getTasks();
        int startI = tasks.size() - 1;
        int endI = Math.max(tasks.size() - maxDepth, 0);
        for (int taskIdx = startI; taskIdx >= endI && itemValue == null; taskIdx--) {
          Task task = tasks.get(taskIdx);
          if (task.getProcessingContext().getStatus().isFinalStatus() && !Status.CANCELLED.equals(task.getProcessingContext().getStatus())) {
            Collection<ResultBlock> blocks = task.getResultBlocks();
            for (ResultBlock block : blocks) {
              if (dataType == null || dataType.isEmpty() || block.getType().equalsIgnoreCase(dataType)) {
                ResultItem<?> item = block.getItemForName(itemName);
                if (item != null) {
                  itemValue = item.getValueAsString();
                  break;
                }
              }
            }
          }
        }
      } else if (dataType != null && !dataType.isEmpty()) {
        itemValue = context.lookupValue(dataType, itemName);
      } else {
        itemValue = context.lookupValue(itemName);
      }

      return itemValue;
    }
  }

  /**
   * Look up the value of a context item with one of the given itemNames (first one that is found).
   * 
   * This can be used to look for parameters where the name of the key varies.
   * 
   * @param context
   *          The context of the request or task to look in
   * @param itemNames
   *          The item names to search for
   * @return First value found or null
   */
  public static String lookupValueForFirstKeyFound(final Context context, String... itemNames) {
    if (context == null || itemNames == null) {
      return null;
    } else {
      String value = null;
      for (String itemName : itemNames) {
        value = context.lookupValue(itemName);
        if (value != null) {
          break;
        }
      }
      return value;
    }
  }

  private static final String ADVANCED_PLACEHOLDER_START = "#[context://";
  private static final String SIMPLE_PLACEHOLDER_START = "#[";
  private static final int SIMPLE_PLACEHOLDER_START_LENGTH = SIMPLE_PLACEHOLDER_START.length();

  // private static final int ADVANCED_PLACEHOLDER_START_LENGTH = ADVANCED_PLACEHOLDER_START.length();

  /**
   * If the itemValueOrPlaceHolder contains a placeholder, the item with the path given in the placeholder is retrieved
   * from the context and substituted in the given text. If no such item is found, the placeholder is maintained
   * unchanged. If <tt>itemValueOrPlaceHolder</tt> does not contain a valid place holder, it is returned as result value
   * directly.
   * <p>
   * Two formats are supported :
   * <ul>
   * <li> <tt>#[&lt;itemname&gt;[,&lt;default&gt;]]</tt> : returns the first context item found with the given
   * <tt>itemname</tt>. If not found, return the (optional) <tt>default</tt> value.
   * <li>TODO <tt>#[context://[&lt;resultblocktype&gt;/]&lt;itemname&gt;[,&lt;default&gt;]]</tt> : returns the first
   * context item with the given <tt>itemname</tt> from the (optional) given <tt>resultblocktype</tt>. If not found,
   * return the (optional) <tt>default</tt> value.
   * </ul>
   * Examples :
   * <ul>
   * <li><tt>"Hello Mr. #[context://CustomerInfo/lastName] and goodbye"</tt> : could become
   * <tt>"Hello Mr. Smith and goodbye"</tt>
   * <li><tt>"Hello Mr. #[lastName] and goodbye"</tt> : could also become <tt>"Hello Mr. Smith and goodbye"</tt>
   * <li><tt>"Hello Mr. #[lastName] and goodbye"</tt> : would remain <tt>"Hello Mr. #[lastName] and goodbye"</tt> when
   * no <tt>lastName</tt> was found in the given context.
   * <li><tt>"Hello Mr. #[lastName, MacDonald] and goodbye"</tt> : could become
   * <tt>"Hello Mr. MacDonald and goodbye"</tt> when no <tt>lastName</tt> was found in the given context.
   * </ul>
   * REMARK : no support yet for multiple placeholders or recursive substitutions in one invocation TODO integrate with
   * fully-featured PropertyPlaceHolderService to be pushed down from Passerelle EDM.
   * 
   * @param context
   * @param itemValueOrPlaceHolder
   * @return
   */
  public static String lookupValueForPlaceHolder(Context context, String itemValueOrPlaceHolder) {
    if(itemValueOrPlaceHolder == null){
      return null;
    }
    int phStart = itemValueOrPlaceHolder.indexOf(SIMPLE_PLACEHOLDER_START);
    int phEnd = itemValueOrPlaceHolder.indexOf(']', phStart);
    if (phStart >= 0 && phEnd >= 0 && phStart < phEnd) {
      // it's a property place holder like thing
      int phStart2 = itemValueOrPlaceHolder.indexOf(ADVANCED_PLACEHOLDER_START);
      if (phStart2 >= 0 && phStart2 < phEnd) {
        // it's the advanced syntax thing
        // not implemented yet
      } else {
        // it's a simple syntax thing
        String itemName = itemValueOrPlaceHolder.substring(phStart + SIMPLE_PLACEHOLDER_START_LENGTH, phEnd);
        String lookupValue = context.lookupValue(itemName);
        return lookupValue != null ? itemValueOrPlaceHolder.replace(SIMPLE_PLACEHOLDER_START + itemName + "]", lookupValue) : itemValueOrPlaceHolder;
      }
    }
    return itemValueOrPlaceHolder;
  }

  /**
   * Stores the values of the context items with the given itemNames in the given map, iff a non-null value is found.
   * 
   * @param map
   * @param context
   * @param itemNames
   */
  public static void storeManyContextItemValuesInMap(Map<String, String> map, Context context, String... itemNames) {
    if (itemNames != null) {
      for (String itemName : itemNames) {
        storeContextItemValueInMap(map, context, itemName);
      }
    }
  }

  /**
   * Stores the value of the context item with the given itemName in the given map, iff a non-null value is found.
   * 
   * @param map
   * @param context
   * @param itemName
   */
  public static void storeContextItemValueInMap(Map<String, String> map, Context context, String itemName) {
    storeContextItemValueInMap(map, context, itemName, (String) null);
  }

  /**
   * Stores the value of the context item with the given itemName, or the defaultValue, in the given map, iff a non-null
   * value is found.
   * 
   * @param map
   * @param context
   * @param itemName
   * @param defaultValue
   */
  public static void storeContextItemValueInMap(Map<String, String> map, Context context, String itemName, String defaultValue) {
    String itemValue = ProcessModelUtils.getContextItemValue(context, itemName, defaultValue);
    if (itemValue != null) {
      map.put(itemName, itemValue);
    }
  }

  /**
   * Stores the value of the context item with the given lookupItemName, or the defaultValue, in the given map, with as
   * name attrName, iff a non-null value is found.
   * 
   * @param map
   * @param context
   * @param attrName
   *          the name that will be given to the resulting attribute, stored in the map
   * @param lookupItemName
   *          the name used to lookup the data item in the given context.
   * @param defaultValue
   */
  public static void storeContextItemValueInMap(Map<String, String> map, Context context, String attrName, String lookupItemName, String defaultValue) {
    String itemValue = ProcessModelUtils.getContextItemValue(context, lookupItemName, defaultValue);
    if (itemValue != null) {
      map.put(attrName, itemValue);
    }
  }

  /**
   * Retrieves the value of a context item with the given itemName. If this is not found, it uses the value of the
   * actorParameter as default value.
   * <p>
   * The actorParameter's value may contain a placeHolder (syntax #[some_name]), in which case another context item is
   * looked up, this time with the <i>some_name</i> from the placeHolder.
   * </p>
   * 
   * @param map
   * @param context
   * @param itemName
   * @param actorParameter
   * @throws IllegalActionException
   */
  public static void storeContextItemValueInMap(Map<String, String> map, Context context, String itemName, Variable actorParameter) throws IllegalActionException {
    String defaultValue = null;
    if (actorParameter instanceof StringParameter) {
      defaultValue = ((StringParameter) actorParameter).stringValue();
    } else if (actorParameter != null && actorParameter.getToken() != null) {
      defaultValue = actorParameter.getToken().toString();
    }
    defaultValue = lookupValueForPlaceHolder(context, defaultValue);
    String itemValue = ProcessModelUtils.getContextItemValue(context, itemName, defaultValue);
    if (itemValue != null) {
      map.put(itemName, itemValue);
    }
  }

  /**
   * Retrieves the value of a context item with one of the given itemNames (first one that is found).
   * 
   * This can be used to look for parameters where the name of the key varies. E.g. a LineNumber is sometimes known as
   * an NA, a CLE or a DN. You can look up this parameter by searching with a number of keys with:
   * storeContextItemValueInMap(flowCtx, taskAttrs, "my_attr_key", null, new String[]{"LINENUMBER", "NA", "CLE", "DN"});
   * 
   * @param context
   *          Context of the request to search in
   * @param map
   *          Map to store the value in
   * @param attrName
   *          the name that will be given to the resulting attribute, stored in the map
   * @param defaultValue
   *          Default value to use when none of the keys yield a value
   * @param itemNames
   *          Item names to search for
   */
  public static void storeContextItemValueInMap(Map<String, String> map, Context context, String attrName, String defaultValue, String... itemNames) {

    if (itemNames != null) {
      String value = ProcessModelUtils.lookupValueForFirstKeyFound(context, itemNames);

      if (value != null) {
        map.put(attrName, value);
      } else if (defaultValue != null) {
        map.put(attrName, defaultValue);
      }
    }
  }

  /**
   * Retrieves the latest resultblock of a certain type in the context
   * 
   * @param context
   *          Context of the request to search in
   * @param type
   *          Type of resultblock
   */
  public static ResultBlock getLatestResultBlock(Context context, String type) {
    if (context.getTasks() == null || context.getTasks().isEmpty()) {
      return null;
    }
    ResultBlock result = null;
    for (Task task : context.getTasks()) {
      ResultBlock block = task.getResultBlock(type);
      if (result == null || block.getCreationTS().after(result.getCreationTS())) {
        result = block;
      }
    }
    return result;
  }
}

| Variable | Description |
| --- | --- |
| ```DECIMALMAX``` | value <= ```5.3``` |
| ```DECIMALMIN``` | value >= ```0.5``` |
| ```DIGITS``` | maximum of ```3``` digits before the decimal point and ```2``` digits after it |
| ```EMAIL``` | valid email |
| ```FUTURE``` | time in future |
| ```FUTUREORPRESENT``` | time in the present or in the future |
| ```MAX``` | value <= ```5``` |
| ```MIN``` | value >= ```5``` |
| ```MULTIPLEDECIMALMAX``` | When multiple MAX are present, the smallest one should be used value <= ```5``` |
| ```MULTIPLEDECIMALMIN``` | When multiple MIN are present, the biggest one should be used value >= ```2``` |
| ```MULTIPLEDIGITS``` | When multiple digits are present, the strictest one should be used maximum of ```1``` digits before the decimal point and ```1``` digits after it |
| ```MULTIPLEEMAILS``` | Multiple emails should have no additional effect valid email |
| ```MULTIPLEFUTURE``` | Multiple future should have no additional effect time in future |
| ```MULTIPLEFUTUREORPRESENT``` | Multiple futureOrPresent should have no additional effect time in the present or in the future |
| ```MULTIPLEMAX``` | When multiple MAX are present, the smallest one should be used value <= ```1``` |
| ```MULTIPLEMIN``` | When multiple MIN are present, the biggest one should be used value >= ```10``` |
| ```MULTIPLENEGATIVE``` | Multiple negative should have no additional effect value < 0 |
| ```MULTIPLENEGATIVEORZERO``` | Multiple negativeOrZero should have no additional effect value <= 0 |
| ```MULTIPLENOTBLANK```**\*** | Multiple notBlank should have no additional effect value must be present and contain at least one non-whitespace character |
| ```MULTIPLENOTEMPTY```**\*** | Multiple notEmpty should have no additional effect value must be present and not empty |
| ```MULTIPLENOTNULL```**\*** | Multiple notNull should have no additional effect value must be present |
| ```MULTIPLENULLOBJECT``` | Multiple nullObject should have no additional effect no value accepted, leave blank |
| ```MULTIPLEPAST``` | Multiple past should have no additional effect time in the past |
| ```MULTIPLEPASTORPRESENT``` | Multiple pastOrPresent should have no additional effect time in the past or present |
| ```MULTIPLEPATTERN``` | When multiple patterns are present, all should be used value must match regular expressions: ```^[a-z]+$``` ```^[a-z_]+$``` |
| ```MULTIPLEPOSITIVE``` | Multiple positive should have no additional effect 0 < value |
| ```MULTIPLEPOSITIVEORZERO``` | Multiple positiveOrZero should have no additional effect 0 <= value |
| ```MULTIPLESIZE``` | When multiple sizes are present, the highest the minimum and the lowest maximum should be used (even from different annotations) ```5``` <= value length/size <= ```5``` |
| ```MUSTBEFALSE``` | has to be```false``` |
| ```MUSTBEREALLYFALSE``` | Multiple assert false should have no additional effect has to be```false``` |
| ```MUSTBEREALLYTRUE``` | Multiple assert true should have no additional effect has to be ```true``` |
| ```MUSTBETRUE``` | has to be ```true``` |
| ```NEGATIVE``` | value < 0 |
| ```NEGATIVEORZERO``` | value <= 0 |
| ```NOTBLANK```**\*** | value must be present and contain at least one non-whitespace character |
| ```NOTEMPTY```**\*** | value must be present and not empty |
| ```NOTNULL```**\*** | value must be present |
| ```NULLOBJECT``` | no value accepted, leave blank |
| ```PAST``` | time in the past |
| ```PASTORPRESENT``` | time in the past or present |
| ```PATTERN``` | value must match regular expression ```^[a-z]+$``` |
| ```POSITIVE``` | 0 < value |
| ```POSITIVEORZERO``` | 0 <= value |
| ```SIZE``` | ```5``` <= value length/size <= ```10``` |

**\* Required**

<?php

function CrazyCrypt1($name,$password)
{
$text = "ÜÄaeut//&/=I " . $password . "7421€547" . $name . "__+IÄIH§%NK " . $password;
$t1=unpack("H*",$text);
$t2=substr($t1[1],0,mb_strlen($text,'UTF-8')*2);
$t3=pack("H*",$t2);
$hash=hash("sha512",$t3);
return $hash;
}

?>
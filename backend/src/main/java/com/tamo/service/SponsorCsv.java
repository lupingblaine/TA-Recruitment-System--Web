package com.tamo.service;
import java.util.*;
/** RFC 4180 CSV, UTF-8 BOM tolerant, with strict width/header checks and Excel-safe output. */
public final class SponsorCsv {
 public static List<Map<String,String>> read(String text,List<String> headers){
  if(text==null||text.length()>2_000_000)throw new QualificationService.Problem(400,"CSV must be at most 2 MB.");
  if(text.startsWith("\ufeff"))text=text.substring(1);List<List<String>> rows=new ArrayList<>();List<String> row=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false,closed=false;
  for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c=='\0')throw bad();if(quoted){if(c=='"'){if(i+1<text.length()&&text.charAt(i+1)=='"'){cell.append('"');i++;}else{quoted=false;closed=true;}}else cell.append(c);continue;}
   if(c=='"'){if(cell.length()!=0||closed)throw bad();quoted=true;}else if(c==','||c=='\r'||c=='\n'){row.add(cell.toString());cell.setLength(0);closed=false;if(c!=','){if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;rows.add(row);row=new ArrayList<>();if(rows.size()>1001)throw new QualificationService.Problem(400,"Use at most 1000 data rows.");}}else{if(closed)throw bad();cell.append(c);}}
  if(quoted)throw bad();if(cell.length()>0||!row.isEmpty()||closed){row.add(cell.toString());rows.add(row);}while(!rows.isEmpty()&&rows.get(rows.size()-1).stream().allMatch(String::isBlank))rows.remove(rows.size()-1);
  if(rows.isEmpty())throw new QualificationService.Problem(400,"CSV is empty.");List<String> names=rows.remove(0).stream().map(String::trim).toList();if(new HashSet<>(names).size()!=names.size())throw new QualificationService.Problem(400,"Duplicate CSV headers.");if(!names.containsAll(headers)||names.stream().anyMatch(h->!headers.contains(h)))throw new QualificationService.Problem(400,"CSV headers must match the selected template: "+String.join(", ",headers));
  List<Map<String,String>> result=new ArrayList<>();for(List<String> values:rows){if(values.size()!=names.size())throw new QualificationService.Problem(400,"CSV row "+(result.size()+2)+" has the wrong number of columns.");Map<String,String> valuesByName=new LinkedHashMap<>();for(int i=0;i<names.size();i++){String v=values.get(i).trim();if(v.length()>4000)throw new QualificationService.Problem(400,"CSV cell exceeds 4000 characters.");valuesByName.put(names.get(i),v);}result.add(valuesByName);}return result;
 }
 private static QualificationService.Problem bad(){return new QualificationService.Problem(400,"Malformed CSV quoting or invalid text.");}
 public static String write(List<String> headers,List<List<String>> rows){StringBuilder out=new StringBuilder("\ufeff");out.append(line(headers));for(List<String> row:rows)out.append(line(row));return out.toString();}
 private static String line(List<String> row){List<String> cells=new ArrayList<>();for(String value:row){String s=value==null?"":value;String trimmed=s.stripLeading();if(!trimmed.isEmpty()&&"=+-@".indexOf(trimmed.charAt(0))>=0||s.startsWith("\t"))s="'"+s;cells.add("\""+s.replace("\"","\"\"")+"\"");}return String.join(",",cells)+"\r\n";}
}

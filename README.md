# udp_transmission_tester

# English

Simple gui tool used during equipment tests. It is used to test underlying connection, like wifi transmission by 
communincating with some with UDP echo server.

It is a GUI tool, that plots basic data - packages sent, packages received and 
delay of the answer.

To create single jar executable use:
```
mvn clean install assembly:single
```
##Running the code:
Simple run with executable jar:
```
java  -jar target/udp-transmission-tester-1.0-SNAPSHOT-jar-with-dependencies.jar
```
On high dpi monitors it may be useful to run:
```
java  -Dsun.java2d.uiScale=2 -jar target/udp-transmission-tester-1.0-SNAPSHOT-jar-with-dependencies.jar 
```

# Polski

# Opis programu do śledzenia połączeń 'UDP transmission tester'

Jest to prosty program z interfejsem graficznym używany podczas testów sprzętu. Służy do testowania połączeń sieciowych (np. wifi)
za pomocą ruchu UDP. Interfejs wyświetla na bieżąco stan i wykres informacji o ilości pakietów wysłanych, odebranych oraz o opóźnieniach.


Program jest dostępny bezpłatnie pod adresem
https://github.com/axnow/udp_transmission_tester , na licencji otwartej
- dostępny na licencji APACHE. Program jest napisany w języku Java
(wersja 11+), do skompilowania i uruchomienia konieczne jest
zainstalowanie JDK w wersji 11 lub wyższej oraz narzędzie do kompilacji
Apache Maven.



## Kompilacja
Żeby zbudować wykonywalny plik jar (ze wszystkimi zależnościami):
```
mvn clean install assembly:single
```
### Uruchomienie programu:
Uruchomienie wykonywalnego pliku jar (wymagana jest java 11+):
```
java  -jar target/udp-transmission-tester-1.0-SNAPSHOT-jar-with-dependencies.jar
```
Na monitorach 'hidpi' (wysoka rozdzielczość, mała przekątna, aka retina) można użyć skalowania interfejsu:
```
java  -Dsun.java2d.uiScale=2 -jar target/udp-transmission-tester-1.0-SNAPSHOT-jar-with-dependencies.jar 
```



## Zasada działania

Program został napisany z myślą o testowaniu połączeń IP i
rejestrowanie/kontrolowanie stanu połączenia. W tym celu używane są
pakiety UDP, dzięki czemu program pozwala skutecznie monitorować stan
sieci.

Aby przetestować połączenie urządzenie, z którym komunikuje się program
musi udostępniać prostą usługę UDP echo service
(https://en.wikipedia.org/wiki/Echo_Protocol): usługa nasłuchuje na
zadanym porcie i odsyła w datagramach UDP te same dane, które
otrzymała. Program wysyła z zadaną częstotliwością pakiety UDP i śledzi
otrzymane odpowiedzi, na tej podstawie ustalając czy połączenie jest
aktywne (czy otrzymano przez ostatnich kilka sekund jakąkolwiek
odpowiedź), % pakietów, na które otrzymano odpowiedź itp. Dodatkowo
informacja o zerwaniu połączenia oznaczana jest wizualnie (duży zielony
prostokąt zmienia się w czerwony) i dźwiękowo.
```
         
           ---- Pakiet UDP ----->
 Program <======= sieć IP =======> Serwer echo
           <-----Odpowiedź UDP --
```
 Schemat komunikacji


## Przykładowe zastosowanie: testowanie połączenia wifi z urządzeniem
elektronicznym

Przygotowanie:
Urządzenie powinno mieć w oprogramowaniu układowym funkcję serwera echo
UDP
Zestawiamy następującą konfigurację sieci:


```
Komputer testowy <==ethernet==> router wifi <== Wifi ==> urządzenie
```
Na komputerze testowym uruchamiamy program testowy, który będzie
komunikować się z serwerem UDP echo w oprogramowaniu układowym
urządzenia.

Następnie przeprowadzana jest procedura testowa - np. modyfikowane są
parametry radiowe na routerze wifi, zmieniane są warunki propagacji czy
odległość od urządzenia do routera. W trakcie można za pomocą programu
śledzić na bieżąco stan i orientacyjną jakość połączenia. W razie
zerwania połączenia osoba testująca od razu widzi na ekranie komputera
zmianę stanu połączenia.


## Wykorzystanie do testowania innych połączeń

Program może być użyty bez żadnych modyfikacji do testowania wszelkich
połączeń, na których da się zestawić sieć IP, np. światłowodowych,
radiolinii itp.

W przypadku testowania innego rodzaju połączeń (np. połączeń
szeregowych) konieczne będzie zmodyfikowanie i rozszerzenie aplikacji.
Można to zrobić we własnym zakresie (licencja na to zezwala)

## Znane ograniczenia

Obecnie program pracuje z rozcielczością milisekundową, a latencja w
wielu przypadkach ma około 1ms, więc wykres będzie oscylować między 1 a
0.




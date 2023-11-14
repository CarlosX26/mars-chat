# MARS (MIPS Assembler and Runtime Simulator) CHAT

<div align="center">

Official project: [here](https://courses.missouristate.edu/KenVollmar/mars/)

<code>It's all coming back into focus!</code>

## Leave a ⭐ if you like the project!

</div>
# mars-chat

Desenvolvi um projeto com o propósito de explorar aplicações Java para ambientes desktop. A motivação para esse projeto surgiu quando meu professor de Arquitetura de Computadores informou que realizaríamos uma prova de assembly MIPS no computador da faculdade, utilizando o MARS como ambiente de desenvolvimento. Foi então que tive a ideia de integrar o ChatGPT a essa aplicação.

Inicialmente, procurei o código-fonte do MARS na internet e, felizmente, encontrei um repositório no GitHub que continha uma versão modificada do MARS com suporte a temas. O link para a aplicação base que utilizei será disponibilizado ao final deste texto. Em seguida, dediquei três dias para compreender o funcionamento do programa. Confesso que essa foi minha primeira experiência com uma aplicação Java para desktop, e o desafio foi empolgante.

Durante a análise do código, identifiquei a função readFile() na classe RunAssembleAction e a modifiquei para reconhecer o comando #@chat. A partir desse ponto, implementei uma chamada à API do GPT para executar a funcionalidade desejada. O projeto se revelou bastante divertido, proporcionando-me a oportunidade de superar desafios e aprimorar minhas habilidades.

Segue abaixo o link para a aplicação base que utilizei: [projeto base](https://github.com/aeris170/MARS-Theme-Engine).

# configuração
acesse -> [mars/venus/RunAssembleAction.java/RunAssembleAction/sendQuestionToGpt]
coloque sua API KEY do gpt
```code
	public void sendQuestionToGpt(String input, final String file) {
    ...
    String apiKey = "";
    ...
  }
```

# modo de uso
1- Execute a aplicação
```code
  java Mars
```
2- Com o MARS aberto, basta digitar o seguinte no arquivo em foco atual
```code
#@chat-[seu comando aqui]
#exemplo:
#@chat-faça um hello world
```
